# Plant ID — Image Upload & Deduplication API

Spring Boot backend for a plant-identification app: JWT-authenticated photo uploads stored in S3, perceptual-hash duplicate detection via a Python microservice, AI identification with Gemini, Postgres persistence. Time-boxed presigned URLs keep the bucket private.

## Architecture
```text
Client ──► Spring Boot :8080 ──► JWT filter ──► PhotoController
                                                    │
                                              PhotoService
                               ┌────────────────────┼────────────────────┐
                               ▼                    ▼                    ▼
                     Flask dHash svc          Gemini 1.5 Flash      AWS S3 (private)
                      (PIL, :5000)            (identify plant)     photos/{UUID}.jpg
                               │                                     │
                 existsByHash? ──► 400                      presigned GET URLs
                 existsByScientificName? ──► 400             (15 min, per request)
```


**Tech:** Java 17 · Spring Boot (Web, Security, Data JPA) · PostgreSQL · AWS S3 (SDK v2, S3Presigner) · Python/Flask + PIL (dHash microservice) · Gemini 1.5 Flash

## Upload pipeline

`POST /api/photos/upload` (authenticated; user resolved from the JWT subject, never a client-supplied ID):

1. File is forwarded to the dHash microservice → 128-bit perceptual hash
2. `existsByHash` — exact duplicate → `400`, **before any AI call is made**
3. Gemini 1.5 Flash identifies the plant: scientific name, common name, one-sentence description
4. `existsByScientificName` — species already in the user's collection → `400`
5. MIME type validated; uploaded to S3 as `photos/{UUID}.{ext}` — the **key** is persisted, never a URL
6. `Photo` row persisted (original filename, S3 key, user FK, names, AI description, hash, timestamp)
7. Response includes a 15-minute presigned URL for viewing the image

## Authentication

Stateless JWT auth via Spring Security:

- `POST /api/auth/register` — rejects duplicate emails, BCrypt-encodes the password, persists the user, and returns a signed JWT immediately (no separate login step)
- `POST /api/auth/login` — `AuthenticationManager` verifies credentials; on success returns the JWT plus expiry and user info (`401` on bad credentials)
- `JWTAuthenticationFilter` (a `OncePerRequestFilter` registered ahead of `UsernamePasswordAuthenticationFilter`) extracts the Bearer token, verifies the HMAC-SHA256 signature, loads the user, and populates the `SecurityContext`
- No sessions (`SessionCreationPolicy.STATELESS`) and CSRF disabled — there are no cookies to forge, so there's nothing for CSRF to protect
- Tokens carry the email as subject, 1-hour expiry, signed with a Base64-decoded 256-bit key from config

Every protected endpoint resolves the user from the security context — never from a client-supplied ID — so a caller can't act as another user.

## Design decisions

- **Dedup before the AI call.** The hash check runs before Gemini is ever touched — a duplicate upload costs one cheap hash computation instead of an AI API call. The pipeline is ordered by cost.
- **Two layers of dedup, different guarantees.** Exact-image hash catches re-uploads; scientific name catches "different photo, same plant." The collection stays meaningful at both levels.
- **Perceptual hash, not SHA-256.** dHash is designed for images: today it does exact matching, but the same hashes support Hamming-distance similarity search later (BK-tree — already a TODO in the code). A cryptographic hash could never do that.
- **dHash as a separate microservice.** Image processing lives in Python (PIL); the API stays in Java. HTTP boundary, independently deployable, each side uses the right tool.
- **Private bucket, presigned reads.** The bucket is never public. Viewing goes through 15-minute presigned GET URLs minted per request — time-boxed access without exposing AWS credentials or opening the bucket.
- **Store the key, not the URL.** Presigned URLs expire, so persisting one would rot. The database holds the stable S3 key; URLs are minted fresh on every read. Storage identity and access grants stay decoupled.
- **UUID keys + MIME validation.** User-supplied filenames never touch storage (no collisions, no traversal); only image MIME types accepted, validated at the boundary.
- **DTOs over entities.** `PhotoDto` keeps JPA entities with lazy `User` associations from leaking into JSON responses.
- **JWT, email as subject, 1h expiry.** Stateless auth; every protected endpoint resolves the user from the security context.

## API

| Method | Endpoint | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | no | Register → 201 + JWT (409 on duplicate) |
| POST | `/api/auth/login` | no | Login → JWT (401 on bad credentials) |
| POST | `/api/photos/upload` | yes | Upload photo; dedup → identify → store in S3 |
| GET | `/api/photos/user/photos` | yes | User's collection, newest first |
| POST | `:5000/hasher` | — | dHash microservice: image → 128-bit hash |

Photo responses include `photoUrl` — a presigned S3 URL valid for 15 minutes.

## Running locally

1. Postgres running; `.env` with `SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD`, `JWT_SECRET_KEY`, `gemini.api-key`
2. AWS: `AWS_REGION` (defaults to `us-west-2`), `AWS_S3_BUCKET`, and credentials via the default credential chain
3. `python python-service/image-hasher.py` (Flask on `:5000`)
4. `cd spring-backend && ./mvnw spring-boot:run` (`:8080`)
5. Register → login → `POST /api/photos/upload` with the Bearer token

Note: `ddl-auto=create-drop` — the schema is recreated on each boot. Dev setting, not prod.

## Limitations / what I'd do next

- **Uploads still proxy through Spring.** Bytes flow client → app server → S3, so upload bandwidth and large-file memory pressure sit on the API tier. The next step is presigned PUT URLs: the backend mints a short-lived upload URL, the client PUTs straight to S3, and the app server leaves the data path entirely (the pipeline would reorder — hash and identify from the S3 object after upload).
- **Presigned URL per photo per request.** Signing is local crypto (no network round trip), so it's cheap — but the list endpoint mints N URLs per call.
- **Exact-match dedup only.** A resized or re-encoded copy produces a different hash; Hamming-distance search over the hash column is the next step.
- **Synchronous Gemini call on the upload path.** Identification latency is user-facing; under load this belongs on a queue.
- **No upload rate limiting.** Every upload triggers a Gemini API call — an open cost vector.
- **Hasher URL is hardcoded** (`http://127.0.0.1:5000`); should be configuration.

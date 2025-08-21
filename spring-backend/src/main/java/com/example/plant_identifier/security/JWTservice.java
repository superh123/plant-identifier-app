package com.example.plant_identifier.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Generate JWT tokens and validate them
@Service
public class JWTservice {

    @Value("${security.jwt.expiration-time}")
    private Long expirationTime;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    //extracts username from a jwt payload (email in our case)
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    //extract expiration time from a jwt payload
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //extracts a generic claim from a jwt payload
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claim = extractAllClaims(token);
        return claimsResolver.apply(claim);
    }

    //extracts all claims from a jwt payload and verify signature
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //used if we're trying to build a jwt with extraClaims
    public String generateToken(Map<String, Object> extraClaims,
                                MyUserDetails userDetails){
        return constructToken(extraClaims, userDetails, expirationTime);
    }

    //overloaded generateToken method, used if jwt has no extra claims
    public String generateToken(MyUserDetails userDetails){
        return constructToken(new HashMap<String, Object>(), userDetails, expirationTime);
    }

    //jwt service builds a token with header, payload and signature
    private String constructToken(Map<String, Object> extraClaims,
                              MyUserDetails userDetails,
                              Long jwtExpirationTime) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSigningKey(){
        //take base 64 encoded key and decode it to 32 byte binary (256-bit)
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        //create secret key object suitable for HMAC-SHA signing key algorithims
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //check if subject of jwt token equal to that of authentication session/database
    //also check if token past expiry date
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);

        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    //calculate if jwt token expiration past already
    private boolean isTokenExpired(String token) {

        Date expiry = extractExpiration(token);

        return expiry.before(new Date());

    }

    public Long getExpirationTime() {
        return expirationTime;
    }

}

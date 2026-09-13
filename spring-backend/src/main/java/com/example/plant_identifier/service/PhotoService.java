package com.example.plant_identifier.service;

import com.example.plant_identifier.entities.Photo;
import com.example.plant_identifier.entities.User;
import com.example.plant_identifier.repositories.PhotoRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final FileService fileService;
    private final GeminiPlantService geminiPlantService;
    private final WebClient webClient;

    public PhotoService(PhotoRepository photoRepository, FileService fileService, GeminiPlantService geminiPlantService, WebClient.Builder webClientBuilder) {
        this.photoRepository = photoRepository;
        this.fileService = fileService;
        this.geminiPlantService = geminiPlantService;
        this.webClient = webClientBuilder.build();
    }

    private MultiValueMap < String, HttpEntity<?> > buildMultiPartBody(MultipartFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .contentType(MediaType.parseMediaType(file.getContentType()));
        return builder.build();
    }

    public Photo savePhoto(MultipartFile file, User user) throws IOException {

        //Make call to python dhash microservice
        //Check if image hash sent is identical to any hash in database, if yes throw an error
        //Saves api call to AI service
        //TO-DO: PERHAPS EXPAND THIS TO SEARCH DATABASE HASHES FOR SIMILARITY? USE BK-TREE PERHAPS?

        String hash = webClient.post()
                .uri("http://127.0.0.1:5000/hasher")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(buildMultiPartBody(file)))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println(hash);

        boolean photo_already_exist = photoRepository.existsByHash(hash);

        if (photo_already_exist) {
            throw new IllegalArgumentException("Plant already exists in your collection!");
        }

        // Get plant names and description from AI service
        List<String> photo_results = geminiPlantService.generateContent(file);

        boolean plant_already_present = photoRepository.existsByScientificName(photo_results.get(0));

        // Check if user already has that plant in their collection
        if (plant_already_present) {
            throw new IllegalArgumentException("Plant already exists in your collection!");
        }

        //Uploads file to AWS S3 and returns the S3 object key
        String s3Key = fileService.saveFile(file);

        //Create a new photo object, to store in database
        Photo photo = new Photo(
                file.getOriginalFilename(),
                s3Key,
                user,
                photo_results.get(0),
                photo_results.get(1),
                photo_results.get(2),
                hash);

        return photoRepository.save(photo);
    }

    public List<Photo> getUserPhotos(User user){
        return photoRepository.findByUserOrderByTakenAtDesc(user);
    }








}

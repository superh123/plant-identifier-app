package com.example.plant_identifier.controllers;

import com.example.plant_identifier.dto.PhotoDto;
import com.example.plant_identifier.entities.Photo;
import com.example.plant_identifier.entities.User;
import com.example.plant_identifier.repositories.UserRepository;
import com.example.plant_identifier.service.FileService;
import com.example.plant_identifier.service.PhotoService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final UserRepository userRepository;
    private final FileService fileService;
    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);

    public PhotoController(PhotoService photoService, UserRepository userRepository, FileService fileService) {
        this.photoService = photoService;
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file
    ){
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // email is the subject from our jwt
            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Photo savedPhoto = photoService.savePhoto(file, user);

            PhotoDto photoDto = new PhotoDto(savedPhoto);
            photoDto.setPhotoUrl(fileService.getPresignedUrl(savedPhoto.getS3Key()));

            return ResponseEntity.ok(photoDto);

        } catch (IOException e) {
            logger.error("Photo failed to save", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/photos") ResponseEntity<List<PhotoDto>> getUserPhotos(){

        //Get currently logged-in user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Photo> userPhotos = photoService.getUserPhotos(user);

        List<PhotoDto> photoDtos = userPhotos.stream()
                .map(photo -> {
                    PhotoDto dto = new PhotoDto(photo);
                    dto.setPhotoUrl(fileService.getPresignedUrl(photo.getS3Key()));
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(photoDtos);

    }









}

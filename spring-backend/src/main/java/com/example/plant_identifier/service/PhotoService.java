package com.example.plant_identifier.service;

import com.example.plant_identifier.entities.Photo;
import com.example.plant_identifier.entities.User;
import com.example.plant_identifier.repositories.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final FileService fileService;

    public PhotoService(PhotoRepository photoRepository, FileService fileService) {
        this.photoRepository = photoRepository;
        this.fileService = fileService;
    }

    public Photo savePhoto(MultipartFile file, User user) throws IOException {

        //TODO : Check if same photo already in database, then don't save photo and
        // notify user that plant found already
        //HOW? Get AI to check if same plant name exists for given user ID!

        //Stores file locally and returns unique filename
        String filename = fileService.saveFile(file);

        //Gives us the file URL on our system
        //String fileURL = fileService.getFileUrl(filename);

        //Create a new photo object, to store in database
        Photo photo = new Photo(file.getOriginalFilename(), filename, user);

        return photoRepository.save(photo);
    }

    public List<Photo> getUserPhotos(User user){
        return photoRepository.findByUserOrderByTakenAtDesc(user);
    }








}

package com.example.plant_identifier.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    //where files will be uploaded for now
    private final String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${server.port}")
    private String serverPort;


    //Create directory if not already there when backend booted up
    public FileService() throws IOException {

        // Get absolute path for user home directory
        String userHome = System.getProperty("user.home");
        System.out.println(userHome);

        this.uploadDir = userHome + File.separator + "uploads" + File.separator + "photos" + File.separator;

        // Alternative: Use temp directory?
        // String tempDir = System.getProperty("java.io.tmpdir");
        // this.uploadDir = tempDir + File.separator + "app-uploads" + File.separator + "photos" + File.separator;


        try {
            Files.createDirectories(Paths.get(uploadDir));
            System.out.println("Upload directory created at: " + uploadDir);
        } catch (IOException exception){
            throw new RuntimeException("Could not create upload directory");
        }
    }

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()){
            throw new IllegalArgumentException("File is empty");
        }

        //Validate file type
        String contentType = file.getContentType();
        if (!validImageType(contentType)){
            throw new IllegalArgumentException("Invalid image type. Only images allowed");
        }

        //Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        //Save file
        Path filePath = Paths.get(uploadDir + uniqueFilename);
        file.transferTo(filePath.toFile());

        return uniqueFilename;
    }

    private boolean validImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/gif")
        );
    }

    public String getFileUrl(String filename){
        // Return full absolute URL instead of relative path
        return baseUrl + ":" + serverPort + "/uploads/photos/" + filename;
    }

    //used in FileUploadConfig to quickly access a photo
    public String getUploadDir() {
        return uploadDir;
    }


}

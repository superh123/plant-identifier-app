package com.example.plant_identifier.config;

import com.example.plant_identifier.service.FileService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    private final FileService fileService;

    public FileUploadConfig(FileService fileService) {
        this.fileService = fileService;
    }

    //Allows us to get the uploaded files from our storage
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String uploadPath = fileService.getUploadDir();

        System.out.println("Configuring resource handler to serve files from: " + uploadPath);

        //Serve uploaded files from uploads directory
        registry.addResourceHandler("/uploads/photos/**")
                .addResourceLocations("file:" + uploadPath);
    }

}

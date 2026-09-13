package com.example.plant_identifier.dto;

import com.example.plant_identifier.entities.Photo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PhotoDto {
    private Long photoId;
    private String filename;
    private String aiDescription;
    private String plantSpecies;
    private LocalDateTime takenAt;

    // Temporary presigned S3 URL for viewing the image; set after construction
    // since generating it requires calling out to FileService.
    private String photoUrl;

    public PhotoDto(Photo photo) {
        this.photoId = photo.getPhotoId();
        this.aiDescription = photo.getAiDescription();
        this.plantSpecies = photo.getPlantSpecies();
        this.takenAt = photo.getTakenAt();
        this.filename = photo.getFilename();
    }
}

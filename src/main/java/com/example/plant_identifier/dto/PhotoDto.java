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
    private String filepath;
    private String aiDescription;
    private String plantSpecies;
    private LocalDateTime takenAt;

    public PhotoDto(Photo photo) {
        this.photoId = photo.getPhotoId();
        this.aiDescription = photo.getAiDescription();
        this.plantSpecies = photo.getPlantSpecies();
        this.takenAt = photo.getTakenAt();
        this.filepath = photo.getFilepath();
        this.filename = photo.getFilename();
    }
}

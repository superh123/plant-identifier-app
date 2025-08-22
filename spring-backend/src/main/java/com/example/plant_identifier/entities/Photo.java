package com.example.plant_identifier.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.convert.DataSizeUnit;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Data
public class Photo {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long photoId;

    @NotBlank
    //what the user names the file e.g. happy.jpg
    private String filename;

    @NotBlank
    //the uuid file path + ext e.g. UUID.jpg
    private String filepath;

    //the description the AI service will return
    private String aiDescription;

    private String scientificName;

    private String commonName;

    private String hash;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Photo() {}


    public Photo(String filename, String filepath, User user,
                 String scientificName, String commonName, String aiDescription, String hash) {
        this.filename = filename;
        this.filepath = filepath;
        this.user = user;
        this.scientificName = scientificName;
        this.commonName = commonName;
        this.aiDescription = aiDescription;
        this.hash = hash;
        this.takenAt = LocalDateTime.now();
    }














}

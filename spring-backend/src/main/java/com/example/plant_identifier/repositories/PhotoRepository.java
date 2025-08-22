package com.example.plant_identifier.repositories;

import com.example.plant_identifier.entities.Photo;
import com.example.plant_identifier.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByUser(User user);
    List<Photo> findByUserOrderByTakenAtDesc(User user); //find photo from newest to oldest hence, 'descending'
    boolean existsByScientificName(String scientificName);
    boolean existsByHash(String hash);

}

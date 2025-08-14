package com.example.plant_identifier.controllers;

import com.example.plant_identifier.entities.User;
import com.example.plant_identifier.repositories.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String test() {
        return "backend is working!";
    }

    @GetMapping("/db")
    public String testDatabase(){
        long userCount = userRepository.count();
        return "Database connected! User count: " + userCount;
    }

    @GetMapping("/user")
    public User createTestUser(){
        User testUser = new User("testuser", "testuser@gmail.com", "ksdie*");
        return userRepository.save(testUser);
    }

    @GetMapping("/user2")
    public User createTestUser2(){
        User testUser = new User("testuser2", "testuser2@gmail.com", "ksdie**");
        return userRepository.save(testUser);
    }


}

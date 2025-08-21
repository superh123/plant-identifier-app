package com.example.plant_identifier.security;

import com.example.plant_identifier.entities.User;
import com.example.plant_identifier.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

//How do I find a user?
//Uses database to search for user presented in jwt token
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public MyUserDetails loadUserByUsername(String email){
        System.out.println("Looking up email: " + email);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        System.out.println("User found: " + optionalUser.isPresent());
        User user = optionalUser.orElseThrow(() -> new RuntimeException("Could not find user"));

//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Could not find user"));

        return new MyUserDetails(user); //wrap the entity as a 'MyUserDetails' object for spring security
    }
}

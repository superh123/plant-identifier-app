package com.example.plant_identifier.security;

import com.example.plant_identifier.dto.AuthenticationResponseDto;
import com.example.plant_identifier.dto.LoginDto;
import com.example.plant_identifier.dto.RegisterDto;
import com.example.plant_identifier.entities.User;
import com.example.plant_identifier.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTservice jwtService;


    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTservice jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponseDto login(LoginDto request) {
        try {
            // Verify user exists and password is correct, else catch bad credentials exception
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Get the users details from the authentication object
            // Cast to MyUserDetails since .getPrincipal returns a generic Object
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

            String token = jwtService.generateToken(userDetails);

            // Build the AuthenticationResponseDto to return info of verified user
            return AuthenticationResponseDto.builder()
                    .token(token)
                    .expiresIn(jwtService.getExpirationTime())
                    .tokenType("Bearer")
                    .user(
                            AuthenticationResponseDto.UserInfo.builder()
                                    .id(userDetails.getId())
                                    .username(userDetails.getUsersName())
                                    .email(userDetails.getEmail())
                                    .build()
                    )
                    .message("Login successful!")
                    .build();

        } catch (BadCredentialsException e) {
            return AuthenticationResponseDto.builder()
                    .error("Invalid email or password, please try again!")
                    .build();
        }
    }

    public AuthenticationResponseDto register(RegisterDto request) {
        try {
            // We have to check if the user exists already
            if (userRepository.existsByEmail(request.getEmail())) {
                return AuthenticationResponseDto.builder()
                        .error("User already exists with email: " + request.getEmail())
                        .build();
            }

            //Create User entity with RegisterDto
            User newUser = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .build();

            //JPA returns managed entity with new fields like id
            User savedUser = userRepository.save(newUser);

            //Generate tokens
            MyUserDetails userDetails = new MyUserDetails(savedUser);
            String token = jwtService.generateToken(userDetails);

            return AuthenticationResponseDto.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(AuthenticationResponseDto.UserInfo.builder()
                            .id(userDetails.getId())
                            .username(userDetails.getUsersName())
                            .email(userDetails.getEmail())
                            .build())
                    .message("Registration successful")
                    .build();

        } catch (Exception e) {
            return AuthenticationResponseDto.builder()
                    .error("Registration failed: " + e.getMessage())
                    .build();
        }

    }





}

package com.example.plant_identifier.security;

import com.example.plant_identifier.dto.AuthenticationResponseDto;
import com.example.plant_identifier.dto.LoginDto;
import com.example.plant_identifier.dto.RegisterDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@RequestBody @Valid LoginDto request) {
        try {

            AuthenticationResponseDto response = authenticationService.login(request);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponseDto.builder()
                            .error("Invalid credentials")
                            .build());

        }

    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponseDto> register(@RequestBody @Valid RegisterDto request) {
        try {

            AuthenticationResponseDto response = authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e){

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthenticationResponseDto.builder()
                            .error(e.getMessage())
                            .build());
        }
    }




}

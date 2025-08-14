package com.example.plant_identifier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    String email;

    @Size(min = 6, message = "Password should be at least 6 letters long" )
    @NotBlank(message = "Password is required")
    String password;



}

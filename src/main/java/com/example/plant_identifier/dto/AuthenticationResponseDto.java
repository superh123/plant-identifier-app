package com.example.plant_identifier.dto;

import com.example.plant_identifier.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDto {

    // JWT access token
    private String token;

    // READ-ME: Add refresh token when needed

    // Token type (Bearer)
    @Builder.Default
    private String tokenType = "Bearer";

    // Token expiration time in seconds
    private Long expiresIn;

    // User information to return
    private UserInfo user;

    // Success message
    private String message;

    // Error message (in case auth failed)
    private String error;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;

        //Return user info without sensitive information
        //Static so we don't have to have an instance of UserInfo to use method
        public static UserInfo fromUser(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();

        }
    }





}

package com.example.plant_identifier.config;

import com.example.plant_identifier.security.JWTAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class SecurityConfig {
    private final AuthenticationProvider authProvider;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(AuthenticationProvider authProvider, JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.authProvider = authProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //disable since stateless, Cross-Site Request Forgery occurs more often with sessions and cookies
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("api/auth/**", "/uploads/photos/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                //we want our jwtAuthFilter to occur before the default filter for login form credential processing
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    //Add CorsConfigurationSource









}

package com.example.plant_identifier.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Security;

//Filter JWT tokens, extends Spring Security OncePerRequestFilter which intercepts every HTTP request
@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter{

    private final JWTservice jwtService;

    private final MyUserDetailsService userDetailsService;

    public JWTAuthenticationFilter(JWTservice jwtService, MyUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                                 @NonNull HttpServletResponse response,
                                                 @NonNull FilterChain filterChain) throws ServletException, IOException {

        //check if JWT present through "Authorization" header of request
        String authHeader = request.getHeader("Authorization");

        //if null or doesn't start with bearer, pass response and request onto next filter
        //as jwt filter only responsible for authenticating requests with a token
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //extract jwt from auth header
            String jwt = authHeader.substring(7);

            String userEmail = jwtService.extractUsername(jwt);
            System.out.println("JWT subject/email: " + userEmail);


            //access currently logged-in user, can use to check if they're authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            //if there is a user email field, and a currently logged-in user, we can compare them
            if (userEmail != null && authentication == null) {

                //load user from database with UserDetailsService
                MyUserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                //if the token is valid for the current security context holder
                if (jwtService.isTokenValid(jwt, userDetails)){

                    //authentication token creation to represent authenticated user to Spring security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    //add extra information to authtoken like IP and session ID (if any)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                }
            }

            //pass request and response to next filter in chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("Exception in JWT filter: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }

}

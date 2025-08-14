package com.example.plant_identifier.security;


import com.example.plant_identifier.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

//Define what a 'user' is in spring security context
public class MyUserDetails implements UserDetails {
    private final User user;

    public MyUserDetails(User user){
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    //for authentication return email
    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return UserDetails.super.isAccountNonExpired(); } //true

    @Override
    public boolean isAccountNonLocked() { return UserDetails.super.isAccountNonLocked(); } //true

    @Override
    public boolean isCredentialsNonExpired() { return UserDetails.super.isCredentialsNonExpired(); } //true

    @Override
    public boolean isEnabled() { return UserDetails.super.isEnabled(); } //true

    //custom getters to get credentials

    public Long getId() { return user.getId(); }

    public String getUsersName() { return user.getUsername(); }

    public String getEmail() { return user.getEmail(); }

}

package com.gym.crm.core.security;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class JwtTokenAuthentication implements Authentication {

    private final boolean authenticated;
    private final UserDetails userDetails;

    @Getter
    private String token;

    private JwtTokenAuthentication(String token) {
        this.token = token;
        this.authenticated = false;
        this.userDetails = null;
    }

    private JwtTokenAuthentication(UserDetails userDetails) {
        this.userDetails = userDetails;
        this.authenticated = true;
    }

    public static JwtTokenAuthentication authenticated(UserDetails userPrincipal) {
        return new JwtTokenAuthentication(userPrincipal);
    }

    public static JwtTokenAuthentication unauthenticated(String token) {
        return new JwtTokenAuthentication(token);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Optional.ofNullable(userDetails)
                .map(UserDetails::getAuthorities)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public UserDetails getPrincipal() {
        return userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Not supported, use constructor");
        }
    }

    @Override
    public String getName() {
        return Optional.ofNullable(userDetails)
                .map(UserDetails::getUsername)
                .orElse(null);
    }

    public void clearCredentials() {
        this.token = null;
    }

}
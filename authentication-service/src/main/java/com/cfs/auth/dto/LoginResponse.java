package com.cfs.auth.dto;

import java.util.Set;

public class LoginResponse {

    private Long userId;

    private String email;

    private String token;

    private String tokenType;

    private long expirationMs;

    private Set<String> roles;


    // No-argument constructor
    public LoginResponse() {
    }


    // All-argument constructor
    public LoginResponse(
            Long userId,
            String email,
            String token,
            String tokenType,
            long expirationMs,
            Set<String> roles) {

        this.userId = userId;
        this.email = email;
        this.token = token;
        this.tokenType = tokenType;
        this.expirationMs = expirationMs;
        this.roles = roles;
    }


    // Getters

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public Set<String> getRoles() {
        return roles;
    }


    // Setters

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
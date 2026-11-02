package com.cfs.auth.dto;

import java.util.Set;

public class RegisterResponse {

    private Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private Set<String> roles;

    private String message;


    // No-argument constructor
    public RegisterResponse() {
    }


    // All-argument constructor
    public RegisterResponse(
            Long userId,
            String firstName,
            String lastName,
            String email,
            Set<String> roles,
            String message) {

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.message = message;
    }


    // Getters

    public Long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getMessage() {
        return message;
    }


    // Setters

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

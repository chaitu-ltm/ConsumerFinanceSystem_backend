//package com.cfs.auth.dto;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class CreateInternalUserRequest {
//
//    @NotBlank(message = "First name is required")
//    private String firstName;
//
//    private String lastName;
//
//    @NotBlank(message = "Email is required")
//    @Email(message = "Please enter valid email")
//    private String email;
//
//    @NotBlank(message = "Password is required")
//    @Size(
//        min = 8,
//        message = "Password must be at least 8 characters"
//    )
//    private String password;
//
//    @NotBlank(message = "Role is required")
//    private String role;
//}


package com.cfs.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateInternalUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
        min = 8,
        message = "Password must be at least 8 characters"
    )
    private String password;

    @NotBlank(message = "Role is required")
    private String role;


    // No-argument constructor
    public CreateInternalUserRequest() {
    }


    // All-argument constructor
    public CreateInternalUserRequest(
            String firstName,
            String lastName,
            String email,
            String password,
            String role) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }


    // Getters

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }


    // Setters

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
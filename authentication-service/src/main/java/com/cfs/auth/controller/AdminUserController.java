package com.cfs.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost
        .PreAuthorize;

import org.springframework.web.bind.annotation
        .PostMapping;

import org.springframework.web.bind.annotation
        .RequestBody;

import org.springframework.web.bind.annotation
        .RequestMapping;

import org.springframework.web.bind.annotation
        .RestController;

import com.cfs.auth.dto
        .CreateInternalUserRequest;

import com.cfs.auth.dto.RegisterResponse;

import com.cfs.auth.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RegisterResponse>
            createInternalUser(

            @Valid
            @RequestBody
            CreateInternalUserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    authService
                        .createInternalUser(
                            request
                        )
                );
    }
}
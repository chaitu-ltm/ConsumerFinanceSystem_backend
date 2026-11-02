package com.cfs.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.cfs.auth.dto.LoginRequest;
import com.cfs.auth.dto.LoginResponse;

import com.cfs.auth.dto.RegisterRequest;
import com.cfs.auth.dto.RegisterResponse;

import com.cfs.auth.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> registerCustomer(

			@Valid @RequestBody RegisterRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCustomer(request));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(

			@Valid @RequestBody LoginRequest request) {

		return ResponseEntity.ok(authService.login(request));
	}
}
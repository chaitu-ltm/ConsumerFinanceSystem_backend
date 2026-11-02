package com.cfs.auth.service;

import com.cfs.auth.dto.CreateInternalUserRequest;

import com.cfs.auth.dto.LoginRequest;
import com.cfs.auth.dto.LoginResponse;

import com.cfs.auth.dto.RegisterRequest;
import com.cfs.auth.dto.RegisterResponse;

public interface AuthService {

	RegisterResponse registerCustomer(RegisterRequest request);

	LoginResponse login(LoginRequest request);

	RegisterResponse createInternalUser(CreateInternalUserRequest request);
}
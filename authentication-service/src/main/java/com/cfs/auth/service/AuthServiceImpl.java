package com.cfs.auth.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.cfs.auth.dto.CreateInternalUserRequest;

import com.cfs.auth.dto.LoginRequest;
import com.cfs.auth.dto.LoginResponse;

import com.cfs.auth.dto.RegisterRequest;
import com.cfs.auth.dto.RegisterResponse;

import com.cfs.auth.entity.Role;
import com.cfs.auth.entity.User;

import com.cfs.auth.exception.ConflictException;

import com.cfs.auth.exception.ResourceNotFoundException;

import com.cfs.auth.repository.RoleRepository;

import com.cfs.auth.repository.UserRepository;

import com.cfs.auth.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	private final JwtService jwtService;

	public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {

		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Override
	public RegisterResponse registerCustomer(RegisterRequest request) {

		return createUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(),
				"ROLE_CUSTOMER", "Customer registered successfully");
	}

	@Override
	public LoginResponse login(LoginRequest request) {

		String email = request.getEmail().trim().toLowerCase();

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		String token = jwtService.generateToken(user);

		return new LoginResponse(user.getId(), user.getEmail(), token, "Bearer", jwtService.getExpirationMs(),
				getRoleNames(user));
	}

	@Override
	public RegisterResponse createInternalUser(CreateInternalUserRequest request) {

		String role = request.getRole().trim().toUpperCase();

		Set<String> allowedRoles = Set.of("ROLE_LOAN_OFFICER", "ROLE_CREDIT_ANALYST", "ROLE_CREDIT_MANAGER",
				"ROLE_ADMIN");

		if (!allowedRoles.contains(role)) {

			throw new ConflictException("Invalid internal role");
		}

		return createUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(),
				role, "Internal user created successfully");
	}

	private RegisterResponse createUser(String firstName, String lastName, String email, String password,
			String roleName, String message) {

		String normalizedEmail = email.trim().toLowerCase();

		if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {

			throw new ConflictException("Email is already registered");
		}

		Role role = roleRepository.findByName(roleName)
				.orElseThrow(() -> new ResourceNotFoundException(roleName + " is not configured"));

		User user = new User();

		user.setFirstName(firstName.trim());

		user.setLastName(lastName == null ? null : lastName.trim());

		user.setEmail(normalizedEmail);

		user.setPassword(passwordEncoder.encode(password));

		user.setEnabled(true);

		user.setRoles(Set.of(role));

		User savedUser = userRepository.save(user);

		return new RegisterResponse(savedUser.getId(), savedUser.getFirstName(), savedUser.getLastName(),
				savedUser.getEmail(), getRoleNames(savedUser), message);
	}

	private Set<String> getRoleNames(User user) {

		return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
	}
}
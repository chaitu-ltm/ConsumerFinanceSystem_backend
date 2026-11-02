package com.cfs.auth.config;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.CommandLineRunner;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import com.cfs.auth.entity.Role;
import com.cfs.auth.entity.User;

import com.cfs.auth.repository.RoleRepository;
import com.cfs.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final RoleRepository roleRepository;

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	@Value("${security.bootstrap-admin.email}")
	private String adminEmail;

	@Value("${security.bootstrap-admin.password}")
	private String adminPassword;

	@Value("${security.bootstrap-admin.first-name}")
	private String adminFirstName;

	@Value("${security.bootstrap-admin.last-name}")
	private String adminLastName;

	@Override
	public void run(String... args) {

		createRoles();

		createInitialAdmin();
	}

	private void createRoles() {

		List<String> roleNames = List.of("ROLE_CUSTOMER", "ROLE_LOAN_OFFICER", "ROLE_CREDIT_ANALYST",
				"ROLE_CREDIT_MANAGER", "ROLE_ADMIN");

		for (String roleName : roleNames) {

			if (!roleRepository.existsByName(roleName)) {

				Role role = new Role();

				role.setName(roleName);

				roleRepository.save(role);
			}
		}
	}

	private void createInitialAdmin() {

		if (userRepository.existsByEmailIgnoreCase(adminEmail)) {

			return;
		}

		Role adminRole = roleRepository.findByName("ROLE_ADMIN")
				.orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not configured"));

		User admin = new User();

		admin.setFirstName(adminFirstName);

		admin.setLastName(adminLastName);

		admin.setEmail(adminEmail.trim().toLowerCase());

		admin.setPassword(passwordEncoder.encode(adminPassword));

		admin.setEnabled(true);

		admin.setRoles(Set.of(adminRole));

		userRepository.save(admin);
	}
}
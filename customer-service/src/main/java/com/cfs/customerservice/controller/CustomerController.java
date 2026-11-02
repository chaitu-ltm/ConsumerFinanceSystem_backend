package com.cfs.customerservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.customerservice.dto.AddressRequest;
import com.cfs.customerservice.dto.AddressResponse;
import com.cfs.customerservice.dto.BankAccountRequest;
import com.cfs.customerservice.dto.BankAccountResponse;
import com.cfs.customerservice.dto.CustomerRequest;
import com.cfs.customerservice.dto.CustomerResponse;
import com.cfs.customerservice.dto.DocumentRequest;
import com.cfs.customerservice.dto.DocumentResponse;
import com.cfs.customerservice.dto.EmploymentRequest;
import com.cfs.customerservice.dto.EmploymentResponse;
import com.cfs.customerservice.dto.KycStatusRequest;
import com.cfs.customerservice.dto.OnboardingStatusResponse;
import com.cfs.customerservice.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {

		this.customerService = customerService;
	}

	// =====================================================
	// CUSTOMER PROFILE
	// =====================================================

	@PostMapping
	public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {

		CustomerResponse response = customerService.createCustomer(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}")
	public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.getCustomer(customerId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {

		return ResponseEntity.ok(customerService.getCustomerByUserId(userId));
	}

	// =====================================================
	// ADDRESS
	// =====================================================

	@PostMapping("/{customerId}/addresses")
	public ResponseEntity<AddressResponse> addAddress(

			@PathVariable Long customerId,

			@Valid @RequestBody AddressRequest request) {

		AddressResponse response = customerService.addAddress(customerId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}/addresses")
	public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.getAddresses(customerId));
	}

	// =====================================================
	// EMPLOYMENT
	// =====================================================

	@PostMapping("/{customerId}/employment")
	public ResponseEntity<EmploymentResponse> addEmployment(

			@PathVariable Long customerId,

			@Valid @RequestBody EmploymentRequest request) {

		EmploymentResponse response = customerService.addEmployment(customerId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}/employment")
	public ResponseEntity<EmploymentResponse> getEmployment(@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.getEmployment(customerId));
	}

	// =====================================================
	// BANK ACCOUNT
	// =====================================================

	@PostMapping("/{customerId}/bank-accounts")
	public ResponseEntity<BankAccountResponse> addBankAccount(

			@PathVariable Long customerId,

			@Valid @RequestBody BankAccountRequest request) {

		BankAccountResponse response = customerService.addBankAccount(customerId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}/bank-accounts")
	public ResponseEntity<List<BankAccountResponse>> getBankAccounts(@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.getBankAccounts(customerId));
	}

	// =====================================================
	// DOCUMENTS
	// =====================================================

	@PostMapping("/{customerId}/documents")
	public ResponseEntity<DocumentResponse> addDocument(

			@PathVariable Long customerId,

			@Valid @RequestBody DocumentRequest request) {

		DocumentResponse response = customerService.addDocument(customerId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{customerId}/documents")
	public ResponseEntity<List<DocumentResponse>> getDocuments(@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.getDocuments(customerId));
	}

	// =====================================================
	// KYC
	// =====================================================

	@PatchMapping("/{customerId}/kyc-status")
	public ResponseEntity<CustomerResponse> updateKycStatus(

			@PathVariable Long customerId,

			@Valid @RequestBody KycStatusRequest request) {

		return ResponseEntity.ok(customerService.updateKycStatus(customerId, request));
	}

	@PostMapping("/{customerId}/kyc-review")
	public ResponseEntity<CustomerResponse> requestKycReview(@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.requestKycReview(customerId));
	}

	// =====================================================
	// ONBOARDING STATUS
	// =====================================================

	@GetMapping("/{customerId}/onboarding-status")
	public ResponseEntity<OnboardingStatusResponse> getOnboardingStatus(

			@PathVariable Long customerId) {

		return ResponseEntity.ok(customerService.getOnboardingStatus(customerId));
	}
}
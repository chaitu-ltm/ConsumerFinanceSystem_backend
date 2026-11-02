package com.cfs.emi.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.emi.dto.RepaymentRequest;
import com.cfs.emi.dto.RepaymentResponse;
import com.cfs.emi.service.RepaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emi/repayments")
public class RepaymentController {

	private final RepaymentService service;

	public RepaymentController(RepaymentService service) {
		this.service = service;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	public ResponseEntity<RepaymentResponse> recordRepayment(@Valid @RequestBody RepaymentRequest request,
			Principal principal) {

		return ResponseEntity.status(HttpStatus.CREATED).body(service.recordRepayment(request, principal.getName()));
	}

	@GetMapping("/application/{applicationId}")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','LOAN_OFFICER')")
	public ResponseEntity<List<RepaymentResponse>> getByApplicationId(@PathVariable Long applicationId) {

		return ResponseEntity.ok(service.getByApplicationId(applicationId));
	}

	@GetMapping("/{repaymentId}")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','LOAN_OFFICER')")
	public ResponseEntity<RepaymentResponse> getById(@PathVariable Long repaymentId) {

		return ResponseEntity.ok(service.getById(repaymentId));
	}
}
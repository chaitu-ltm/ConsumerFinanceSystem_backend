package com.cfs.emi.controller;

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

import com.cfs.emi.dto.GenerateEmiScheduleRequest;
import com.cfs.emi.dto.EmiInstallmentResponse;
import com.cfs.emi.dto.LoanAccountResponse;
import com.cfs.emi.service.EmiScheduleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emi")
public class EmiScheduleController {

	private final EmiScheduleService service;

	public EmiScheduleController(EmiScheduleService service) {
		this.service = service;
	}

	@PostMapping("/schedules")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<LoanAccountResponse> generateSchedule(
			@Valid @RequestBody GenerateEmiScheduleRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(service.generateSchedule(request));
	}

	@GetMapping("/schedules/application/{applicationId}")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','LOAN_OFFICER')")
	public ResponseEntity<LoanAccountResponse> getByApplicationId(@PathVariable Long applicationId) {

		return ResponseEntity.ok(service.getByApplicationId(applicationId));
	}

	@GetMapping("/schedules/{loanAccountId}/installments")
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','LOAN_OFFICER')")
	public ResponseEntity<List<EmiInstallmentResponse>> getInstallments(@PathVariable Long loanAccountId) {

		return ResponseEntity.ok(service.getInstallments(loanAccountId));
	}
}
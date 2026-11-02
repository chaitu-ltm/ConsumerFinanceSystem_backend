package com.cfs.emi.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(CustomerLookupUnavailableException.class)
	public ResponseEntity<Map<String, Object>> handleCustomerUnavailable(CustomerLookupUnavailableException ex) {
		return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
	}

	@ExceptionHandler(DisbursementLookupUnavailableException.class)
	public ResponseEntity<Map<String, Object>> handleDisbursementUnavailable(
			DisbursementLookupUnavailableException ex) {
		return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
	}

	@ExceptionHandler({ LoanAccountNotFoundException.class, EmiInstallmentNotFoundException.class })
	public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {

		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DuplicateEmiScheduleException.class)
	public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateEmiScheduleException ex) {

		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(DisbursementNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleDisbursementNotFound(DisbursementNotFoundException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(DisbursementVerificationException.class)
	public ResponseEntity<Map<String, Object>> handleDisbursementMismatch(DisbursementVerificationException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(InvalidEmiScheduleRequestException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidSchedule(InvalidEmiScheduleRequestException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(AlreadyPaidRepaymentException.class)
	public ResponseEntity<Map<String, Object>> handleAlreadyPaid(AlreadyPaidRepaymentException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(InvalidRepaymentException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidRepayment(InvalidRepaymentException ex) {

		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

		Map<String, String> validation = new LinkedHashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> validation.put(error.getField(), error.getDefaultMessage()));

		Map<String, Object> response = new LinkedHashMap<>();

		response.put("timestamp", LocalDateTime.now());

		response.put("status", 400);
		response.put("error", "Bad Request");

		response.put("message", "Validation failed");

		response.put("validationErrors", validation);

		return ResponseEntity.badRequest().body(response);
	}

	private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {

		Map<String, Object> response = new LinkedHashMap<>();

		response.put("timestamp", LocalDateTime.now());

		response.put("status", status.value());

		response.put("error", status.getReasonPhrase());

		response.put("message", message);

		return ResponseEntity.status(status).body(response);
	}
}
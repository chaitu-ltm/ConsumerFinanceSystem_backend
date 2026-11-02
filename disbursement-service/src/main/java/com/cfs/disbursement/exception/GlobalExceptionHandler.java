package com.cfs.disbursement.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApprovalUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleApprovalUnavailable(
            ApprovalUnavailableException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage(), request.getRequestURI());
    }

    /*
     * --------------------------------------------------
     * DISBURSEMENT NOT FOUND
     * --------------------------------------------------
     */
    @ExceptionHandler(DisbursementNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleDisbursementNotFound(
            DisbursementNotFoundException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * --------------------------------------------------
     * DUPLICATE DISBURSEMENT
     * --------------------------------------------------
     */
    @ExceptionHandler(DuplicateDisbursementException.class)
    public ResponseEntity<Map<String, Object>>
    handleDuplicateDisbursement(
            DuplicateDisbursementException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * --------------------------------------------------
     * LOAN NOT APPROVED
     * --------------------------------------------------
     */
    @ExceptionHandler(LoanNotApprovedException.class)
    public ResponseEntity<Map<String, Object>>
    handleLoanNotApproved(
            LoanNotApprovedException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    /*
     * --------------------------------------------------
     * REQUEST VALIDATION
     * --------------------------------------------------
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body =
                createErrorBody(
                        HttpStatus.BAD_REQUEST,
                        "Request validation failed",
                        request.getRequestURI()
                );

        body.put(
                "validationErrors",
                validationErrors
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    /*
     * --------------------------------------------------
     * APPROVAL SERVICE 404 THROUGH FEIGN
     * --------------------------------------------------
     */
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>>
    handleFeignNotFound(
            FeignException.NotFound ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Approval decision not found for the application",
                request.getRequestURI()
        );
    }

    /*
     * --------------------------------------------------
     * DOWNSTREAM SERVICE FAILURE
     * --------------------------------------------------
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>>
    handleFeignException(
            FeignException ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to communicate with Approval Service",
                request.getRequestURI()
        );
    }

    /*
     * --------------------------------------------------
     * GENERAL EXCEPTION
     * --------------------------------------------------
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI()
        );
    }

    /*
     * --------------------------------------------------
     * HELPER METHODS
     * --------------------------------------------------
     */

    private ResponseEntity<Map<String, Object>>
    buildErrorResponse(
            HttpStatus status,
            String message,
            String path) {

        return ResponseEntity
                .status(status)
                .body(
                        createErrorBody(
                                status,
                                message,
                                path
                        )
                );
    }

    private Map<String, Object> createErrorBody(
            HttpStatus status,
            String message,
            String path) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                status.value()
        );

        body.put(
                "error",
                status.getReasonPhrase()
        );

        body.put(
                "message",
                message
        );

        body.put(
                "path",
                path
        );

        return body;
    }
}
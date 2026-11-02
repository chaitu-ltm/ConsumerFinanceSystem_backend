package com.cfs.credit.controller;

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

import com.cfs.credit.dto.request.CreditAssessmentRequest;
import com.cfs.credit.dto.response.CreditAssessmentResponse;
import com.cfs.credit.entity.AssessmentStatus;
import com.cfs.credit.service.CreditAssessmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/credit-assessments")
public class CreditAssessmentController {

    private final CreditAssessmentService service;

    public CreditAssessmentController(
            CreditAssessmentService service) {

        this.service = service;
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('CREDIT_ANALYST', 'CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<CreditAssessmentResponse>
    createAssessment(
            @Valid
            @RequestBody
            CreditAssessmentRequest request,
            Principal principal) {

        CreditAssessmentResponse response =
                service.createAssessment(
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{creditAssessmentId}")
    @PreAuthorize(
            "hasAnyRole('CREDIT_ANALYST', 'CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<CreditAssessmentResponse>
    getById(
            @PathVariable
            Long creditAssessmentId) {

        return ResponseEntity.ok(
                service.getById(
                        creditAssessmentId
                )
        );
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize(
            "hasAnyRole('CREDIT_ANALYST', 'CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<CreditAssessmentResponse>
    getByApplicationId(
            @PathVariable
            Long applicationId) {

        return ResponseEntity.ok(
                service.getByApplicationId(
                        applicationId
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('CREDIT_ANALYST', 'CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<List<CreditAssessmentResponse>>
    getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/status/{status}")
    @PreAuthorize(
            "hasAnyRole('CREDIT_ANALYST', 'CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<List<CreditAssessmentResponse>>
    getByStatus(
            @PathVariable
            AssessmentStatus status) {

        return ResponseEntity.ok(
                service.getByStatus(status)
        );
    }
}
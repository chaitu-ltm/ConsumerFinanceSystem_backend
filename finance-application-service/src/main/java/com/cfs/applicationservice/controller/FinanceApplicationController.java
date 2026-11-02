package com.cfs.applicationservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.applicationservice.dto.FinanceApplicationRequest;
import com.cfs.applicationservice.dto.ApplicationAssessmentContextResponse;
import com.cfs.applicationservice.dto.FinanceApplicationResponse;
import com.cfs.applicationservice.dto.StatusHistoryResponse;

import com.cfs.applicationservice.service.FinanceApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class FinanceApplicationController {

    private final FinanceApplicationService
            financeApplicationService;


    public FinanceApplicationController(
            FinanceApplicationService financeApplicationService) {

        this.financeApplicationService =
                financeApplicationService;
    }


    @PostMapping
    public ResponseEntity<FinanceApplicationResponse>
            createApplication(

            @Valid
            @RequestBody
            FinanceApplicationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    financeApplicationService
                        .createApplication(request)
                );
    }


    @GetMapping("/{applicationId}/assessment-context")
    public ResponseEntity<ApplicationAssessmentContextResponse> getAssessmentContext(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(financeApplicationService.getAssessmentContext(applicationId));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<FinanceApplicationResponse>
            getApplication(

            @PathVariable
            Long applicationId) {

        return ResponseEntity.ok(
                financeApplicationService
                    .getApplication(applicationId)
        );
    }


    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FinanceApplicationResponse>>
            getCustomerApplications(

            @PathVariable
            Long customerId) {

        return ResponseEntity.ok(
                financeApplicationService
                    .getApplicationsByCustomer(
                        customerId
                    )
        );
    }


    @PostMapping("/{applicationId}/submit")
    public ResponseEntity<FinanceApplicationResponse>
            submitApplication(

            @PathVariable
            Long applicationId) {

        return ResponseEntity.ok(
                financeApplicationService
                    .submitApplication(
                        applicationId
                    )
        );
    }


    @GetMapping("/{applicationId}/history")
    public ResponseEntity<List<StatusHistoryResponse>>
            getStatusHistory(

            @PathVariable
            Long applicationId) {

        return ResponseEntity.ok(
                financeApplicationService
                    .getStatusHistory(
                        applicationId
                    )
        );
    }
}
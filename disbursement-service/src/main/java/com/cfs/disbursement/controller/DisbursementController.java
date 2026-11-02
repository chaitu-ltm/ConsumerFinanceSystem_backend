package com.cfs.disbursement.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cfs.disbursement.dto.request.DisbursementRequest;
import com.cfs.disbursement.dto.response.DisbursementResponse;
import com.cfs.disbursement.service.DisbursementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/disbursements")
public class DisbursementController {

    private final DisbursementService service;

    public DisbursementController(
            DisbursementService service) {

        this.service = service;
    }

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<DisbursementResponse>
    createDisbursement(
            @Valid
            @RequestBody
            DisbursementRequest request,
            Principal principal) {

        DisbursementResponse response =
                service.createDisbursement(
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<List<DisbursementResponse>>
    getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }

    @GetMapping("/{disbursementId}")
    @PreAuthorize(
            "hasAnyRole('CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<DisbursementResponse>
    getById(
            @PathVariable Long disbursementId) {

        return ResponseEntity.ok(
                service.getById(disbursementId)
        );
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize(
            "hasAnyRole('CREDIT_MANAGER', 'ADMIN')"
    )
    public ResponseEntity<DisbursementResponse>
    getByApplicationId(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                service.getByApplicationId(applicationId)
        );
    }
}
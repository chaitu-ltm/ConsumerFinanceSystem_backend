package com.cfs.customerservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.customerservice.dto.AdminKycResponse;
import com.cfs.customerservice.dto.KycActionResponse;
import com.cfs.customerservice.dto.KycRejectRequest;
import com.cfs.customerservice.service.AdminKycService;

@RestController
@RequestMapping("/api/admin/kyc")
public class AdminKycController {

    private final AdminKycService adminKycService;

    public AdminKycController(
            AdminKycService adminKycService) {

        this.adminKycService = adminKycService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<AdminKycResponse>>
            getPendingKyc() {

        return ResponseEntity.ok(
                adminKycService.getPendingKyc()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{customerId}")
    public ResponseEntity<AdminKycResponse>
            getKycDetails(
                    @PathVariable Long customerId) {

        return ResponseEntity.ok(
                adminKycService
                        .getKycDetails(customerId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{customerId}/verify")
    public ResponseEntity<KycActionResponse>
            verifyKyc(
                    @PathVariable Long customerId) {

        return ResponseEntity.ok(
                adminKycService
                        .verifyKyc(customerId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{customerId}/reject")
    public ResponseEntity<KycActionResponse>
            rejectKyc(
                    @PathVariable Long customerId,
                    @RequestBody
                    KycRejectRequest request) {

        return ResponseEntity.ok(
                adminKycService
                        .rejectKyc(
                                customerId,
                                request
                        )
        );
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<AdminKycResponse>>
            getAllKyc() {

        return ResponseEntity.ok(
                adminKycService.getAllKyc()
        );
    }
}
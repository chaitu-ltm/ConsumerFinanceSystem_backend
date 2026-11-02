package com.cfs.approval.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.approval.dto.request.ApprovalDecisionRequest;
import com.cfs.approval.dto.response.ApprovalDecisionResponse;
import com.cfs.approval.entity.ApprovalStatus;
import com.cfs.approval.service.ApprovalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping
    public ResponseEntity<ApprovalDecisionResponse> createDecision(
            @Valid @RequestBody ApprovalDecisionRequest request) {

        ApprovalDecisionResponse response =
                approvalService.createDecision(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ApprovalDecisionResponse> getDecisionById(
            @PathVariable Long approvalId) {

        return ResponseEntity.ok(
                approvalService.getDecisionById(approvalId)
        );
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApprovalDecisionResponse>
    getDecisionByApplicationId(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                approvalService.getDecisionByApplicationId(
                        applicationId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ApprovalDecisionResponse>>
    getAllDecisions() {

        return ResponseEntity.ok(
                approvalService.getAllDecisions()
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApprovalDecisionResponse>>
    getDecisionsByStatus(
            @PathVariable ApprovalStatus status) {

        return ResponseEntity.ok(
                approvalService.getDecisionsByStatus(status)
        );
    }

    @GetMapping("/reviewer")
    public ResponseEntity<List<ApprovalDecisionResponse>>
    getDecisionsByReviewer(
            @RequestParam String reviewedBy) {

        return ResponseEntity.ok(
                approvalService.getDecisionsByReviewer(reviewedBy)
        );
    }
}
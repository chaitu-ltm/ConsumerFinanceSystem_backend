package com.cfs.approval.service;


import java.util.List;

import com.cfs.approval.dto.request.ApprovalDecisionRequest;
import com.cfs.approval.dto.response.ApprovalDecisionResponse;
import com.cfs.approval.entity.ApprovalStatus;

public interface ApprovalService {

    ApprovalDecisionResponse createDecision(
            ApprovalDecisionRequest request
    );

    ApprovalDecisionResponse getDecisionById(Long approvalId);

    ApprovalDecisionResponse getDecisionByApplicationId(
            Long applicationId
    );

    List<ApprovalDecisionResponse> getAllDecisions();

    List<ApprovalDecisionResponse> getDecisionsByStatus(
            ApprovalStatus status
    );

    List<ApprovalDecisionResponse> getDecisionsByReviewer(
            String reviewedBy
    );
}
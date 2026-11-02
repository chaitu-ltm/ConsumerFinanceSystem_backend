package com.cfs.approval.mapper;

import org.springframework.stereotype.Component;

import com.cfs.approval.dto.request.ApprovalDecisionRequest;
import com.cfs.approval.dto.response.ApprovalDecisionResponse;
import com.cfs.approval.entity.ApprovalDecision;

@Component
public class ApprovalMapper {

    public ApprovalDecision toEntity(ApprovalDecisionRequest request) {

        ApprovalDecision approval = new ApprovalDecision();

        approval.setApplicationId(request.getApplicationId());
        approval.setCreditAssessmentId(request.getCreditAssessmentId());
        approval.setDecision(request.getDecision());
        approval.setReviewedBy(request.getReviewedBy().trim());

        if (request.getRemarks() != null) {
            approval.setRemarks(request.getRemarks().trim());
        }

        return approval;
    }

    public ApprovalDecisionResponse toResponse(ApprovalDecision approval) {

        ApprovalDecisionResponse response =
                new ApprovalDecisionResponse();

        response.setApprovalId(approval.getApprovalId());
        response.setApplicationId(approval.getApplicationId());
        response.setCreditAssessmentId(
                approval.getCreditAssessmentId()
        );
        response.setDecision(approval.getDecision());
        response.setReviewedBy(approval.getReviewedBy());
        response.setRemarks(approval.getRemarks());
        response.setDecisionDate(approval.getDecisionDate());

        return response;
    }
}
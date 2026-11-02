package com.cfs.approval.dto.request;

import com.cfs.approval.entity.ApprovalStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ApprovalDecisionRequest {

    @NotNull(message = "Application ID is required")
    @Positive(message = "Application ID must be greater than zero")
    private Long applicationId;

    @NotNull(message = "Credit assessment ID is required")
    @Positive(message = "Credit assessment ID must be greater than zero")
    private Long creditAssessmentId;

    @NotNull(message = "Decision is required")
    private ApprovalStatus decision;

    @NotBlank(message = "Reviewer name is required")
    @Size(max = 100, message = "Reviewer name cannot exceed 100 characters")
    private String reviewedBy;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public ApprovalDecisionRequest() {
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getCreditAssessmentId() {
        return creditAssessmentId;
    }

    public void setCreditAssessmentId(Long creditAssessmentId) {
        this.creditAssessmentId = creditAssessmentId;
    }

    public ApprovalStatus getDecision() {
        return decision;
    }

    public void setDecision(ApprovalStatus decision) {
        this.decision = decision;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
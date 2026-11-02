package com.cfs.approval.dto.response;

import java.time.LocalDateTime;
import com.cfs.approval.entity.ApprovalStatus;

public class ApprovalDecisionResponse {

    private Long approvalId;
    private Long applicationId;
    private Long creditAssessmentId;
    private ApprovalStatus decision;
    private String reviewedBy;
    private String remarks;
    private LocalDateTime decisionDate;

    public ApprovalDecisionResponse() {
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
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

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }
}
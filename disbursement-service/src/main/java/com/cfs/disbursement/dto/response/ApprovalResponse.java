package com.cfs.disbursement.dto.response;

public class ApprovalResponse {

    private Long approvalId;
    private Long applicationId;
    private Long creditAssessmentId;
    private String decision;
    private String reviewedBy;
    private String remarks;

    public ApprovalResponse() {
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

    public void setCreditAssessmentId(
            Long creditAssessmentId) {
        this.creditAssessmentId = creditAssessmentId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
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
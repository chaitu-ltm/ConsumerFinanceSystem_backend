package com.cfs.approval.dto.response;

public class CreditAssessmentResponse {

    public enum AssessmentStatus {
        ELIGIBLE,
        NOT_ELIGIBLE
    }

    private Long creditAssessmentId;
    private Long applicationId;
    private AssessmentStatus status;

    public Long getCreditAssessmentId() {
        return creditAssessmentId;
    }

    public void setCreditAssessmentId(Long creditAssessmentId) {
        this.creditAssessmentId = creditAssessmentId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }
}

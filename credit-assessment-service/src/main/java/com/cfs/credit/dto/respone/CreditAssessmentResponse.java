package com.cfs.credit.dto.response;

import java.time.LocalDateTime;

import com.cfs.credit.entity.AssessmentStatus;

public class CreditAssessmentResponse {

    private Long creditAssessmentId;
    private Long applicationId;
    private AssessmentStatus status;
    private String reason;
    private String assessedBy;
    private LocalDateTime assessedAt;

    public CreditAssessmentResponse() {
    }

    public Long getCreditAssessmentId() {
        return creditAssessmentId;
    }

    public void setCreditAssessmentId(
            Long creditAssessmentId) {

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAssessedBy() {
        return assessedBy;
    }

    public void setAssessedBy(String assessedBy) {
        this.assessedBy = assessedBy;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }
}
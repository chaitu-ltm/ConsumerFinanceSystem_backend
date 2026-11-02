package com.cfs.credit.dto.request;

import com.cfs.credit.entity.AssessmentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreditAssessmentRequest {

    @NotNull(message = "Application ID is required")
    @Positive(
            message = "Application ID must be greater than zero"
    )
    private Long applicationId;

    @NotNull(message = "Assessment status is required")
    private AssessmentStatus status;

    @NotBlank(message = "Assessment reason is required")
    @Size(
            min = 5,
            max = 500,
            message = "Reason must contain between 5 and 500 characters"
    )
    private String reason;

    public CreditAssessmentRequest() {
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
}
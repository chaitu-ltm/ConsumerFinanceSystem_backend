package com.cfs.applicationservice.dto;

public class ApplicationAssessmentContextResponse {

    private Long applicationId;
    private String status;

    public ApplicationAssessmentContextResponse() {
    }

    public ApplicationAssessmentContextResponse(Long applicationId, String status) {
        this.applicationId = applicationId;
        this.status = status;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getStatus() {
        return status;
    }
}

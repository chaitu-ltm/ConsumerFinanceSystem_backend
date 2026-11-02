package com.cfs.customerservice.dto;

import jakarta.validation.constraints.NotBlank;

public class KycStatusRequest {

    @NotBlank(message = "KYC status is required")
    private String status;

    private String remarks;

    public KycStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
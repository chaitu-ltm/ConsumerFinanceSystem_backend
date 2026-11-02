package com.cfs.customerservice.dto;

public class KycRejectRequest {

    private String reason;

    public KycRejectRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
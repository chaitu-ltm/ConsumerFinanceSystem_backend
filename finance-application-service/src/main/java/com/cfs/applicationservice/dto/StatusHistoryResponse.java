package com.cfs.applicationservice.dto;

import java.time.LocalDateTime;

public class StatusHistoryResponse {

    private Long historyId;

    private Long applicationId;

    private String oldStatus;

    private String newStatus;

    private String remarks;

    private LocalDateTime changedAt;


    public StatusHistoryResponse() {
    }


    public StatusHistoryResponse(
            Long historyId,
            Long applicationId,
            String oldStatus,
            String newStatus,
            String remarks,
            LocalDateTime changedAt) {

        this.historyId = historyId;
        this.applicationId = applicationId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.changedAt = changedAt;
    }


    public Long getHistoryId() {
        return historyId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
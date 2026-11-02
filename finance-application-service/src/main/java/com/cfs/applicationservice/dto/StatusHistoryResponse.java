package com.cfs.applicationservice.dto;

import java.time.LocalDateTime;

public record StatusHistoryResponse(
        Long historyId,
        Long applicationId,
        String oldStatus,
        String newStatus,
        String remarks,
        LocalDateTime changedAt) {

    public StatusHistoryResponse() {
        this(null, null, null, null, null, null);
    }
}

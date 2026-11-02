package com.cfs.applicationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinanceApplicationResponse(
        Long applicationId,
        String applicationNumber,
        Long customerId,
        Long productId,
        String productName,
        BigDecimal requestedAmount,
        Integer requestedTenureMonths,
        String loanPurpose,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime createdAt) {

    public FinanceApplicationResponse() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }
}

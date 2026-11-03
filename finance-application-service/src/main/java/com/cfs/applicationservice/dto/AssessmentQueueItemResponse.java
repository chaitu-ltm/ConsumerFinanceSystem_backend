package com.cfs.applicationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssessmentQueueItemResponse(
        Long applicationId,
        String applicationNumber,
        Long customerId,
        Long productId,
        String productName,
        BigDecimal requestedAmount,
        Integer requestedTenureMonths,
        String loanPurpose,
        LocalDateTime submittedAt,
        String status) {
}

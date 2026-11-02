package com.cfs.disbursement.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.cfs.disbursement.entity.DisbursementStatus;

public record DisbursementResponse(
        Long disbursementId,
        Long applicationId,
        Long approvalId,
        BigDecimal amount,
        DisbursementStatus status,
        String transactionReference,
        String processedBy,
        LocalDateTime disbursedAt) {
}

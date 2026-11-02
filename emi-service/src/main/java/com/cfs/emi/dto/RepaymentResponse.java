package com.cfs.emi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cfs.emi.entity.PaymentMethod;
import com.cfs.emi.entity.RepaymentStatus;

public record RepaymentResponse(
        Long repaymentId,
        Long loanAccountId,
        Long emiId,
        Integer installmentNumber,
        String paymentReference,
        BigDecimal amount,
        LocalDateTime paymentDate,
        PaymentMethod paymentMethod,
        RepaymentStatus status,
        String recordedBy,
        String remarks) {
}

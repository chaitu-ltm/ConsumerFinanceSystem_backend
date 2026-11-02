package com.cfs.emi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cfs.emi.entity.EmiStatus;

public record EmiInstallmentResponse(
        Long emiId,
        Long loanAccountId,
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal openingPrincipal,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal emiAmount,
        BigDecimal amountPaid,
        EmiStatus status,
        LocalDateTime paidAt) {
}

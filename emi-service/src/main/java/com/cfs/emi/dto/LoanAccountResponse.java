package com.cfs.emi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cfs.emi.entity.LoanAccountStatus;

public record LoanAccountResponse(
        Long loanAccountId,
        Long applicationId,
        Long customerId,
        Long disbursementId,
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal monthlyEmiAmount,
        BigDecimal totalPayableAmount,
        BigDecimal outstandingAmount,
        LocalDate startDate,
        LocalDate maturityDate,
        LoanAccountStatus status) {
}

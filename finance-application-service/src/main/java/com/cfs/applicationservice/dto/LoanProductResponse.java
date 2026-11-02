package com.cfs.applicationservice.dto;

import java.math.BigDecimal;

public record LoanProductResponse(
        Long productId,
        String productCode,
        String productName,
        BigDecimal minimumAmount,
        BigDecimal maximumAmount,
        Integer minimumTenureMonths,
        Integer maximumTenureMonths,
        BigDecimal minimumInterestRate,
        BigDecimal maximumInterestRate,
        boolean active) {

    public LoanProductResponse() {
        this(null, null, null, null, null, null, null, null, null, false);
    }
}

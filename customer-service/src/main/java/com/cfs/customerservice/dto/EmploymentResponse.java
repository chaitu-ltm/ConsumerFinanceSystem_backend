package com.cfs.customerservice.dto;

import java.math.BigDecimal;

public record EmploymentResponse(
        Long employmentId,
        Long customerId,
        String employmentType,
        String employerName,
        String designation,
        BigDecimal monthlyIncome,
        BigDecimal existingMonthlyEmi,
        Integer workExperienceMonths) {

    public EmploymentResponse() {
        this(null, null, null, null, null, null, null, null);
    }
}

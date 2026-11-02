package com.cfs.applicationservice.dto;

import java.math.BigDecimal;

public class LoanProductResponse {

    private Long productId;

    private String productCode;

    private String productName;

    private BigDecimal minimumAmount;

    private BigDecimal maximumAmount;

    private Integer minimumTenureMonths;

    private Integer maximumTenureMonths;

    private BigDecimal minimumInterestRate;

    private BigDecimal maximumInterestRate;

    private boolean active;


    public LoanProductResponse() {
    }


    public LoanProductResponse(
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

        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.minimumTenureMonths = minimumTenureMonths;
        this.maximumTenureMonths = maximumTenureMonths;
        this.minimumInterestRate = minimumInterestRate;
        this.maximumInterestRate = maximumInterestRate;
        this.active = active;
    }


    public Long getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }

    public Integer getMinimumTenureMonths() {
        return minimumTenureMonths;
    }

    public Integer getMaximumTenureMonths() {
        return maximumTenureMonths;
    }

    public BigDecimal getMinimumInterestRate() {
        return minimumInterestRate;
    }

    public BigDecimal getMaximumInterestRate() {
        return maximumInterestRate;
    }

    public boolean isActive() {
        return active;
    }
}
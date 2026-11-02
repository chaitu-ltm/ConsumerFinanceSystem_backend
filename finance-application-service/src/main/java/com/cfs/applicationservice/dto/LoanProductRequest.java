package com.cfs.applicationservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LoanProductRequest {

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(
        value = "1.0",
        message = "Minimum amount must be greater than 0"
    )
    private BigDecimal minimumAmount;

    @NotNull(message = "Maximum amount is required")
    @DecimalMin(
        value = "1.0",
        message = "Maximum amount must be greater than 0"
    )
    private BigDecimal maximumAmount;

    @NotNull(message = "Minimum tenure is required")
    @Positive(message = "Minimum tenure must be positive")
    private Integer minimumTenureMonths;

    @NotNull(message = "Maximum tenure is required")
    @Positive(message = "Maximum tenure must be positive")
    private Integer maximumTenureMonths;

    @NotNull(message = "Minimum interest rate is required")
    @DecimalMin(
        value = "0.0",
        message = "Minimum rate cannot be negative"
    )
    private BigDecimal minimumInterestRate;

    @NotNull(message = "Maximum interest rate is required")
    @DecimalMin(
        value = "0.0",
        message = "Maximum rate cannot be negative"
    )
    private BigDecimal maximumInterestRate;

    private boolean active;


    public LoanProductRequest() {
    }


    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(
            BigDecimal minimumAmount) {

        this.minimumAmount = minimumAmount;
    }


    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(
            BigDecimal maximumAmount) {

        this.maximumAmount = maximumAmount;
    }


    public Integer getMinimumTenureMonths() {
        return minimumTenureMonths;
    }

    public void setMinimumTenureMonths(
            Integer minimumTenureMonths) {

        this.minimumTenureMonths =
                minimumTenureMonths;
    }


    public Integer getMaximumTenureMonths() {
        return maximumTenureMonths;
    }

    public void setMaximumTenureMonths(
            Integer maximumTenureMonths) {

        this.maximumTenureMonths =
                maximumTenureMonths;
    }


    public BigDecimal getMinimumInterestRate() {
        return minimumInterestRate;
    }

    public void setMinimumInterestRate(
            BigDecimal minimumInterestRate) {

        this.minimumInterestRate =
                minimumInterestRate;
    }


    public BigDecimal getMaximumInterestRate() {
        return maximumInterestRate;
    }

    public void setMaximumInterestRate(
            BigDecimal maximumInterestRate) {

        this.maximumInterestRate =
                maximumInterestRate;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
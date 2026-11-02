package com.cfs.applicationservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FinanceApplicationRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(
        value = "1.0",
        message = "Requested amount must be greater than 0"
    )
    private BigDecimal requestedAmount;

    @NotNull(message = "Requested tenure is required")
    @Positive(message = "Requested tenure must be positive")
    private Integer requestedTenureMonths;

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;


    public FinanceApplicationRequest() {
    }


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }


    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(
            BigDecimal requestedAmount) {

        this.requestedAmount = requestedAmount;
    }


    public Integer getRequestedTenureMonths() {
        return requestedTenureMonths;
    }

    public void setRequestedTenureMonths(
            Integer requestedTenureMonths) {

        this.requestedTenureMonths =
                requestedTenureMonths;
    }


    public String getLoanPurpose() {
        return loanPurpose;
    }

    public void setLoanPurpose(String loanPurpose) {
        this.loanPurpose = loanPurpose;
    }
}
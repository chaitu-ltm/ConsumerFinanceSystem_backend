package com.cfs.applicationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinanceApplicationResponse {

    private Long applicationId;

    private String applicationNumber;

    private Long customerId;

    private Long productId;

    private String productName;

    private BigDecimal requestedAmount;

    private Integer requestedTenureMonths;

    private String loanPurpose;

    private String status;

    private LocalDateTime submittedAt;

    private LocalDateTime createdAt;


    public FinanceApplicationResponse() {
    }


    public FinanceApplicationResponse(
            Long applicationId,
            String applicationNumber,
            Long customerId,
            Long productId,
            String productName,
            BigDecimal requestedAmount,
            Integer requestedTenureMonths,
            String loanPurpose,
            String status,
            LocalDateTime submittedAt,
            LocalDateTime createdAt) {

        this.applicationId = applicationId;
        this.applicationNumber = applicationNumber;
        this.customerId = customerId;
        this.productId = productId;
        this.productName = productName;
        this.requestedAmount = requestedAmount;
        this.requestedTenureMonths =
                requestedTenureMonths;
        this.loanPurpose = loanPurpose;
        this.status = status;
        this.submittedAt = submittedAt;
        this.createdAt = createdAt;
    }


    public Long getApplicationId() {
        return applicationId;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public Integer getRequestedTenureMonths() {
        return requestedTenureMonths;
    }

    public String getLoanPurpose() {
        return loanPurpose;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
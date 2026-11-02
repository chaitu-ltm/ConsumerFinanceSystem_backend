package com.cfs.emi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cfs.emi.entity.EmiStatus;

public class EmiInstallmentResponse {

    private Long emiId;
    private Long loanAccountId;
    private Integer installmentNumber;
    private LocalDate dueDate;

    private BigDecimal openingPrincipal;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;

    private BigDecimal emiAmount;
    private BigDecimal amountPaid;

    private EmiStatus status;
    private LocalDateTime paidAt;

    public EmiInstallmentResponse() {
    }

    public Long getEmiId() {
        return emiId;
    }

    public void setEmiId(Long emiId) {
        this.emiId = emiId;
    }

    public Long getLoanAccountId() {
        return loanAccountId;
    }

    public void setLoanAccountId(Long loanAccountId) {
        this.loanAccountId = loanAccountId;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(
            Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getOpeningPrincipal() {
        return openingPrincipal;
    }

    public void setOpeningPrincipal(
            BigDecimal openingPrincipal) {
        this.openingPrincipal = openingPrincipal;
    }

    public BigDecimal getPrincipalComponent() {
        return principalComponent;
    }

    public void setPrincipalComponent(
            BigDecimal principalComponent) {
        this.principalComponent = principalComponent;
    }

    public BigDecimal getInterestComponent() {
        return interestComponent;
    }

    public void setInterestComponent(
            BigDecimal interestComponent) {
        this.interestComponent = interestComponent;
    }

    public BigDecimal getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(BigDecimal emiAmount) {
        this.emiAmount = emiAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public EmiStatus getStatus() {
        return status;
    }

    public void setStatus(EmiStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
package com.cfs.emi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cfs.emi.entity.LoanAccountStatus;

public class LoanAccountResponse {

    private Long loanAccountId;
    private Long applicationId;
    private Long customerId;
    private Long disbursementId;

    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate;
    private Integer tenureMonths;

    private BigDecimal monthlyEmiAmount;
    private BigDecimal totalPayableAmount;
    private BigDecimal outstandingAmount;

    private LocalDate startDate;
    private LocalDate maturityDate;

    private LoanAccountStatus status;

    public LoanAccountResponse() {
    }

    public Long getLoanAccountId() {
        return loanAccountId;
    }

    public void setLoanAccountId(Long loanAccountId) {
        this.loanAccountId = loanAccountId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getDisbursementId() {
        return disbursementId;
    }

    public void setDisbursementId(Long disbursementId) {
        this.disbursementId = disbursementId;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(
            BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(
            BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getMonthlyEmiAmount() {
        return monthlyEmiAmount;
    }

    public void setMonthlyEmiAmount(
            BigDecimal monthlyEmiAmount) {
        this.monthlyEmiAmount = monthlyEmiAmount;
    }

    public BigDecimal getTotalPayableAmount() {
        return totalPayableAmount;
    }

    public void setTotalPayableAmount(
            BigDecimal totalPayableAmount) {
        this.totalPayableAmount = totalPayableAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(
            BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public LoanAccountStatus getStatus() {
        return status;
    }

    public void setStatus(LoanAccountStatus status) {
        this.status = status;
    }
}
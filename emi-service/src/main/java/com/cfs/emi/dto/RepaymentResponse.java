package com.cfs.emi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cfs.emi.entity.PaymentMethod;
import com.cfs.emi.entity.RepaymentStatus;

public class RepaymentResponse {

    private Long repaymentId;
    private Long loanAccountId;
    private Long emiId;

    private Integer installmentNumber;

    private String paymentReference;
    private BigDecimal amount;

    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private RepaymentStatus status;

    private String recordedBy;
    private String remarks;

    public RepaymentResponse() {
    }

    public Long getRepaymentId() {
        return repaymentId;
    }

    public void setRepaymentId(Long repaymentId) {
        this.repaymentId = repaymentId;
    }

    public Long getLoanAccountId() {
        return loanAccountId;
    }

    public void setLoanAccountId(Long loanAccountId) {
        this.loanAccountId = loanAccountId;
    }

    public Long getEmiId() {
        return emiId;
    }

    public void setEmiId(Long emiId) {
        this.emiId = emiId;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(
            Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(
            String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(
            LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(
            PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public RepaymentStatus getStatus() {
        return status;
    }

    public void setStatus(
            RepaymentStatus status) {
        this.status = status;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
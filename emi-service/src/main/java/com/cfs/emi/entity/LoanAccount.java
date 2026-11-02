package com.cfs.emi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_accounts")
public class LoanAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long loanAccountId;

	@Column(nullable = false, unique = true)
	private Long applicationId;

	@Column(nullable = false)
	private Long customerId;

	@Column(nullable = false, unique = true)
	private Long disbursementId;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal principalAmount;

	@Column(nullable = false, precision = 7, scale = 4)
	private BigDecimal annualInterestRate;

	@Column(nullable = false)
	private Integer tenureMonths;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal monthlyEmiAmount;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal totalPayableAmount;

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

	public void setPrincipalAmount(BigDecimal principalAmount) {
		this.principalAmount = principalAmount;
	}

	public BigDecimal getAnnualInterestRate() {
		return annualInterestRate;
	}

	public void setAnnualInterestRate(BigDecimal annualInterestRate) {
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

	public void setMonthlyEmiAmount(BigDecimal monthlyEmiAmount) {
		this.monthlyEmiAmount = monthlyEmiAmount;
	}

	public BigDecimal getTotalPayableAmount() {
		return totalPayableAmount;
	}

	public void setTotalPayableAmount(BigDecimal totalPayableAmount) {
		this.totalPayableAmount = totalPayableAmount;
	}

	public BigDecimal getOutstandingAmount() {
		return outstandingAmount;
	}

	public void setOutstandingAmount(BigDecimal outstandingAmount) {
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal outstandingAmount;

	private LocalDate startDate;

	private LocalDate maturityDate;

	@Enumerated(EnumType.STRING)
	private LoanAccountStatus status;

	private LocalDateTime createdAt;

	
}

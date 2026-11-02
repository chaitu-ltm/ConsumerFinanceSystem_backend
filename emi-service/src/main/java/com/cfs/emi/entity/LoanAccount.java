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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_accounts")
@Getter
@Setter
@NoArgsConstructor
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

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal outstandingAmount;

	private LocalDate startDate;

	private LocalDate maturityDate;

	@Enumerated(EnumType.STRING)
	private LoanAccountStatus status;

	private LocalDateTime createdAt;
}

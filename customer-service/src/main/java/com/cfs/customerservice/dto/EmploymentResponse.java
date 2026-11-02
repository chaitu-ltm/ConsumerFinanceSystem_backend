package com.cfs.customerservice.dto;

import java.math.BigDecimal;

public class EmploymentResponse {

	private Long employmentId;
	private Long customerId;
	private String employmentType;
	private String employerName;
	private String designation;
	private BigDecimal monthlyIncome;
	private BigDecimal existingMonthlyEmi;
	private Integer workExperienceMonths;

	public EmploymentResponse() {
	}

	public EmploymentResponse(Long employmentId, Long customerId, String employmentType, String employerName,
			String designation, BigDecimal monthlyIncome, BigDecimal existingMonthlyEmi, Integer workExperienceMonths) {

		this.employmentId = employmentId;
		this.customerId = customerId;
		this.employmentType = employmentType;
		this.employerName = employerName;
		this.designation = designation;
		this.monthlyIncome = monthlyIncome;
		this.existingMonthlyEmi = existingMonthlyEmi;
		this.workExperienceMonths = workExperienceMonths;
	}

	public Long getEmploymentId() {
		return employmentId;
	}

	public void setEmploymentId(Long employmentId) {
		this.employmentId = employmentId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public String getEmployerName() {
		return employerName;
	}

	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public BigDecimal getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(BigDecimal monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public BigDecimal getExistingMonthlyEmi() {
		return existingMonthlyEmi;
	}

	public void setExistingMonthlyEmi(BigDecimal existingMonthlyEmi) {
		this.existingMonthlyEmi = existingMonthlyEmi;
	}

	public Integer getWorkExperienceMonths() {
		return workExperienceMonths;
	}

	public void setWorkExperienceMonths(Integer workExperienceMonths) {
		this.workExperienceMonths = workExperienceMonths;
	}

}
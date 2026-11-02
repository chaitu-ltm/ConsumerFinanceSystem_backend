package com.cfs.customerservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EmploymentRequest {

    @NotBlank
    private String employmentType;

    private String employerName;

    private String designation;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal monthlyIncome;

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

	@DecimalMin(value = "0.0")
    private BigDecimal existingMonthlyEmi;

    private Integer workExperienceMonths;

    public EmploymentRequest() {
    }

    // Generate getters/setters using STS
}
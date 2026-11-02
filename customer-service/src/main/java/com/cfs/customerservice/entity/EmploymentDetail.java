package com.cfs.customerservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "employment_details")
public class EmploymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "customer_id",
        nullable = false,
        unique = true
    )
    private Long customerId;

    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @Column(name = "employer_name")
    private String employerName;

    private String designation;

    @Column(
        name = "monthly_income",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal monthlyIncome;

    @Column(
        name = "existing_monthly_emi",
        precision = 15,
        scale = 2
    )
    private BigDecimal existingMonthlyEmi;

    @Column(name = "work_experience_months")
    private Integer workExperienceMonths;

    public EmploymentDetail() {
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
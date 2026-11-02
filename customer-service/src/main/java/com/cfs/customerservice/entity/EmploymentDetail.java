package com.cfs.customerservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employment_details")
@Getter
@Setter
@NoArgsConstructor
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

}
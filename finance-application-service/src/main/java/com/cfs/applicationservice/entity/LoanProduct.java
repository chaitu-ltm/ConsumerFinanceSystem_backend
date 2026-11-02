package com.cfs.applicationservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "loan_products")
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "product_code",
        nullable = false,
        unique = true,
        length = 30
    )
    private String productCode;

    @Column(
        name = "product_name",
        nullable = false,
        length = 100
    )
    private String productName;

    @Column(
        name = "minimum_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal minimumAmount;

    @Column(
        name = "maximum_amount",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal maximumAmount;

    @Column(
        name = "minimum_tenure_months",
        nullable = false
    )
    private Integer minimumTenureMonths;

    @Column(
        name = "maximum_tenure_months",
        nullable = false
    )
    private Integer maximumTenureMonths;

    @Column(
        name = "minimum_interest_rate",
        nullable = false,
        precision = 5,
        scale = 2
    )
    private BigDecimal minimumInterestRate;

    @Column(
        name = "maximum_interest_rate",
        nullable = false,
        precision = 5,
        scale = 2
    )
    private BigDecimal maximumInterestRate;

    @Column(nullable = false)
    private boolean active;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LoanProduct() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(BigDecimal maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public Integer getMinimumTenureMonths() {
        return minimumTenureMonths;
    }

    public void setMinimumTenureMonths(
            Integer minimumTenureMonths) {

        this.minimumTenureMonths = minimumTenureMonths;
    }

    public Integer getMaximumTenureMonths() {
        return maximumTenureMonths;
    }

    public void setMaximumTenureMonths(
            Integer maximumTenureMonths) {

        this.maximumTenureMonths = maximumTenureMonths;
    }

    public BigDecimal getMinimumInterestRate() {
        return minimumInterestRate;
    }

    public void setMinimumInterestRate(
            BigDecimal minimumInterestRate) {

        this.minimumInterestRate = minimumInterestRate;
    }

    public BigDecimal getMaximumInterestRate() {
        return maximumInterestRate;
    }

    public void setMaximumInterestRate(
            BigDecimal maximumInterestRate) {

        this.maximumInterestRate = maximumInterestRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
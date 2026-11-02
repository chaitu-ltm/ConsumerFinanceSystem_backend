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
@Table(name = "emi_installments")
@Getter
@Setter
@NoArgsConstructor
public class EmiInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emiId;

    @Column(nullable = false)
    private Long loanAccountId;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal openingPrincipal;

    @Column(precision = 15, scale = 2)
    private BigDecimal principalComponent;

    @Column(precision = 15, scale = 2)
    private BigDecimal interestComponent;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal amountPaid;

    @Enumerated(EnumType.STRING)
    private EmiStatus status;

    private LocalDateTime paidAt;

}
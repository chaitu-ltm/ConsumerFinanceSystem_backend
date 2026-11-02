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
@Table(name = "repayments")
@Getter
@Setter
@NoArgsConstructor
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repaymentId;

    private Long loanAccountId;

    private Long emiId;

    @Column(nullable = false, unique = true)
    private String paymentReference;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private RepaymentStatus status;

    private String recordedBy;

    private String remarks;

}
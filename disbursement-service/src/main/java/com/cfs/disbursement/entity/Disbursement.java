package com.cfs.disbursement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "disbursements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_disbursement_application",
                        columnNames = "application_id"
                )
        }
)
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long disbursementId;

    @Column(
            name = "application_id",
            nullable = false,
            unique = true
    )
    private Long applicationId;

    @Column(
            name = "approval_id",
            nullable = false
    )
    private Long approvalId;

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisbursementStatus status;

    @Column(
            name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 100
    )
    private String transactionReference;

    @Column(
            name = "processed_by",
            nullable = false,
            length = 150
    )
    private String processedBy;

    @Column(
            name = "disbursed_at"
    )
    private LocalDateTime disbursedAt;

}
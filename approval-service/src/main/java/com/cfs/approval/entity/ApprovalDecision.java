package com.cfs.approval.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "approval_decisions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_approval_application",
                        columnNames = "application_id"
                )
        }
)
public class ApprovalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(name = "credit_assessment_id", nullable = false)
    private Long creditAssessmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private ApprovalStatus decision;

    @Column(name = "reviewed_by", nullable = false, length = 100)
    private String reviewedBy;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "decision_date", nullable = false)
    private LocalDateTime decisionDate;

    @PrePersist
    public void prePersist() {
        if (decisionDate == null) {
            decisionDate = LocalDateTime.now();
        }
    }

}
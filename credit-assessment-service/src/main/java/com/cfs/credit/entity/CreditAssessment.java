package com.cfs.credit.entity;

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

@Entity
@Table(
        name = "credit_assessments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credit_assessment_application",
                        columnNames = "application_id"
                )
        }
)
public class CreditAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditAssessmentId;

    @Column(
            name = "application_id",
            nullable = false,
            unique = true
    )
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssessmentStatus status;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(
            name = "assessed_by",
            nullable = false,
            length = 150
    )
    private String assessedBy;

    @Column(
            name = "assessed_at",
            nullable = false
    )
    private LocalDateTime assessedAt;

    public CreditAssessment() {
    }

    @PrePersist
    public void prePersist() {

        if (assessedAt == null) {
            assessedAt = LocalDateTime.now();
        }
    }

    public Long getCreditAssessmentId() {
        return creditAssessmentId;
    }

    public void setCreditAssessmentId(
            Long creditAssessmentId) {

        this.creditAssessmentId = creditAssessmentId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAssessedBy() {
        return assessedBy;
    }

    public void setAssessedBy(String assessedBy) {
        this.assessedBy = assessedBy;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }
}
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

    public ApprovalDecision() {
    }

    @PrePersist
    public void prePersist() {
        if (decisionDate == null) {
            decisionDate = LocalDateTime.now();
        }
    }

    public Long getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(Long approvalId) {
        this.approvalId = approvalId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getCreditAssessmentId() {
        return creditAssessmentId;
    }

    public void setCreditAssessmentId(Long creditAssessmentId) {
        this.creditAssessmentId = creditAssessmentId;
    }

    public ApprovalStatus getDecision() {
        return decision;
    }

    public void setDecision(ApprovalStatus decision) {
        this.decision = decision;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }
}
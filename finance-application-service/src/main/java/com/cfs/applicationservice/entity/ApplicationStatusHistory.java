package com.cfs.applicationservice.entity;

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

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "application_id",
        nullable = false
    )
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "old_status",
        length = 40
    )
    private ApplicationStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "new_status",
        nullable = false,
        length = 40
    )
    private ApplicationStatus newStatus;

    @Column(length = 500)
    private String remarks;

    @Column(
        name = "changed_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime changedAt;

    public ApplicationStatusHistory() {
    }

    @PrePersist
    public void prePersist() {
        changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public ApplicationStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(
            ApplicationStatus oldStatus) {

        this.oldStatus = oldStatus;
    }

    public ApplicationStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(
            ApplicationStatus newStatus) {

        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
package com.cfs.notification.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(nullable = false)
    private Long customerId;

    private Long applicationId;

    private Long approvalId;

    private Long disbursementId;

    private Long repaymentId;

    private Long scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private LocalDateTime readAt;

    @Column(length = 500)
    private String failureReason;

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = NotificationStatus.PENDING;
        }

        if (channel == null) {
            channel = NotificationChannel.IN_APP;
        }
    }

}
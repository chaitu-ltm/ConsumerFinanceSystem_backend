package com.cfs.notification.dto.response;

import java.time.LocalDateTime;

import com.cfs.notification.entity.NotificationChannel;
import com.cfs.notification.entity.NotificationStatus;
import com.cfs.notification.entity.NotificationType;

public record NotificationResponse(
        Long notificationId,
        Long customerId,
        Long applicationId,
        Long approvalId,
        Long disbursementId,
        Long repaymentId,
        Long scheduleId,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        String subject,
        String message,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime readAt) {
}

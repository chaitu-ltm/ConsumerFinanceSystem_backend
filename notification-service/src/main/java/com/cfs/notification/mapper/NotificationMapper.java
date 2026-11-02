package com.cfs.notification.mapper;

import org.springframework.stereotype.Component;

import com.cfs.notification.dto.request.NotificationRequest;
import com.cfs.notification.dto.response.NotificationResponse;
import com.cfs.notification.entity.Notification;

@Component
public class NotificationMapper {

    public Notification toEntity(
            NotificationRequest request) {

        Notification notification =
                new Notification();

        notification.setCustomerId(
                request.getCustomerId()
        );

        notification.setApplicationId(
                request.getApplicationId()
        );

        notification.setApprovalId(
                request.getApprovalId()
        );

        notification.setDisbursementId(
                request.getDisbursementId()
        );

        notification.setRepaymentId(
                request.getRepaymentId()
        );

        notification.setScheduleId(
                request.getScheduleId()
        );

        notification.setType(
                request.getType()
        );

        notification.setChannel(
                request.getChannel()
        );

        notification.setSubject(
                request.getSubject().trim()
        );

        notification.setMessage(
                request.getMessage().trim()
        );

        return notification;
    }

    public NotificationResponse toResponse(
            Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getCustomerId(),
                notification.getApplicationId(),
                notification.getApprovalId(),
                notification.getDisbursementId(),
                notification.getRepaymentId(),
                notification.getScheduleId(),
                notification.getType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getSubject(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getReadAt());
    }
}
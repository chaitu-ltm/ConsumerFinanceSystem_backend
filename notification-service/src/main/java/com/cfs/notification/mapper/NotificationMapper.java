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

        NotificationResponse response =
                new NotificationResponse();

        response.setNotificationId(
                notification.getNotificationId()
        );

        response.setCustomerId(
                notification.getCustomerId()
        );

        response.setApplicationId(
                notification.getApplicationId()
        );

        response.setApprovalId(
                notification.getApprovalId()
        );

        response.setDisbursementId(
                notification.getDisbursementId()
        );

        response.setRepaymentId(
                notification.getRepaymentId()
        );

        response.setScheduleId(
                notification.getScheduleId()
        );

        response.setType(
                notification.getType()
        );

        response.setChannel(
                notification.getChannel()
        );

        response.setStatus(
                notification.getStatus()
        );

        response.setSubject(
                notification.getSubject()
        );

        response.setMessage(
                notification.getMessage()
        );

        response.setCreatedAt(
                notification.getCreatedAt()
        );

        response.setSentAt(
                notification.getSentAt()
        );

        response.setReadAt(
                notification.getReadAt()
        );

        return response;
    }
}
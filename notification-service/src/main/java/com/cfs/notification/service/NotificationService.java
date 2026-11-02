package com.cfs.notification.service;

import java.util.List;

import com.cfs.notification.dto.request.NotificationRequest;
import com.cfs.notification.dto.response.NotificationResponse;
import com.cfs.notification.entity.NotificationType;

public interface NotificationService {

    NotificationResponse createNotification(
            NotificationRequest request
    );

    NotificationResponse getNotification(
            Long notificationId
    );

    List<NotificationResponse> getAllNotifications();

    List<NotificationResponse>
    getNotificationsByCustomer(
            Long customerId
    );

    List<NotificationResponse>
    getNotificationsByCustomerAndType(
            Long customerId,
            NotificationType type
    );

    NotificationResponse markAsRead(
            Long notificationId
    );

    long getUnreadCount(
            Long customerId
    );

    void deleteNotification(
            Long notificationId
    );
}
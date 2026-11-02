package com.cfs.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.notification.entity.Notification;
import com.cfs.notification.entity.NotificationStatus;
import com.cfs.notification.entity.NotificationType;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    List<Notification>
    findByCustomerIdAndStatusOrderByCreatedAtDesc(
            Long customerId,
            NotificationStatus status
    );

    List<Notification>
    findByCustomerIdAndTypeOrderByCreatedAtDesc(
            Long customerId,
            NotificationType type
    );

    long countByCustomerIdAndStatus(
            Long customerId,
            NotificationStatus status
    );
}
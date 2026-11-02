package com.cfs.notification.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.notification.dto.request.NotificationRequest;
import com.cfs.notification.dto.response.NotificationResponse;
import com.cfs.notification.entity.Notification;
import com.cfs.notification.entity.NotificationStatus;
import com.cfs.notification.entity.NotificationType;
import com.cfs.notification.exception.NotificationNotFoundException;
import com.cfs.notification.mapper.NotificationMapper;
import com.cfs.notification.repository.NotificationRepository;
import com.cfs.notification.service.NotificationService;
import com.cfs.notification.security.NotificationOwnership;
import com.cfs.notification.security.NotificationIdentity;

@Service
@Transactional
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository repository;

    private final NotificationMapper mapper;
    private final NotificationOwnership ownership;

    public NotificationServiceImpl(
            NotificationRepository repository,
            NotificationMapper mapper,
            NotificationOwnership ownership) {

        this.repository = repository;
        this.mapper = mapper;
        this.ownership = ownership;
    }

    @Override
    public NotificationResponse createNotification(
            NotificationRequest request) {
        NotificationIdentity.requireAdmin();

        Notification notification =
                mapper.toEntity(request);

        /*
         * SENT records database/inbox availability, not external delivery.
         * Existing channel semantics are preserved; EMAIL/SMS are not delivered.
         */

        notification.setStatus(
                NotificationStatus.SENT
        );

        notification.setSentAt(
                LocalDateTime.now()
        );

        Notification saved =
                repository.save(notification);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(
            Long notificationId) {

        Notification notification =
                findById(notificationId);
        ownership.requireCustomer(notification.getCustomerId());

        return mapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getAllNotifications() {
        NotificationIdentity.requireAdmin();

        return repository
                .findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getNotificationsByCustomer(
            Long customerId) {
        ownership.requireCustomer(customerId);

        return repository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getNotificationsByCustomerAndType(
            Long customerId,
            NotificationType type) {
        ownership.requireCustomer(customerId);

        return repository
                .findByCustomerIdAndTypeOrderByCreatedAtDesc(
                        customerId,
                        type
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(
            Long notificationId) {

        Notification notification =
                findById(notificationId);
        ownership.requireCustomer(notification.getCustomerId());

        notification.setStatus(
                NotificationStatus.READ
        );

        notification.setReadAt(
                LocalDateTime.now()
        );

        Notification updated =
                repository.save(notification);

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(
            Long customerId) {
        ownership.requireCustomer(customerId);

        return repository
                .countByCustomerIdAndStatus(
                        customerId,
                        NotificationStatus.SENT
                );
    }

    @Override
    public void deleteNotification(
            Long notificationId) {

        Notification notification =
                findById(notificationId);
        ownership.requireCustomer(notification.getCustomerId());

        repository.delete(notification);
    }

    private Notification findById(
            Long notificationId) {

        return repository
                .findById(notificationId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with ID: "
                                        + notificationId
                        )
                );
    }
}
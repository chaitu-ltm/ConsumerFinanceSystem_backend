package com.cfs.notification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cfs.notification.dto.request.NotificationRequest;
import com.cfs.notification.dto.response.NotificationResponse;
import com.cfs.notification.entity.NotificationType;
import com.cfs.notification.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(
            NotificationService service) {

        this.service = service;
    }

    /*
     * Manual ADMIN creation. Automatic producer integration is not implemented.
     */
    @PostMapping("/internal")
    public ResponseEntity<NotificationResponse>
    createNotification(
            @Valid @RequestBody
            NotificationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.createNotification(
                                request
                        )
                );
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse>
    getNotification(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                service.getNotification(
                        notificationId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getAllNotifications() {

        return ResponseEntity.ok(
                service.getAllNotifications()
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationResponse>>
    getCustomerNotifications(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                service.getNotificationsByCustomer(
                        customerId
                )
        );
    }

    @GetMapping("/customer/{customerId}/type")
    public ResponseEntity<List<NotificationResponse>>
    getCustomerNotificationsByType(

            @PathVariable Long customerId,

            @RequestParam
            NotificationType type) {

        return ResponseEntity.ok(
                service
                        .getNotificationsByCustomerAndType(
                                customerId,
                                type
                        )
        );
    }

    @GetMapping("/customer/{customerId}/unread-count")
    public ResponseEntity<Map<String, Object>>
    getUnreadCount(
            @PathVariable Long customerId) {

        long count =
                service.getUnreadCount(customerId);

        return ResponseEntity.ok(
                Map.of(
                        "customerId",
                        customerId,

                        "unreadCount",
                        count
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse>
    markAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                service.markAsRead(notificationId)
        );
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void>
    deleteNotification(
            @PathVariable Long notificationId) {

        service.deleteNotification(
                notificationId
        );

        return ResponseEntity.noContent().build();
    }
}
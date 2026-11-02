package com.cfs.notification.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.cfs.notification.entity.NotificationChannel;
import com.cfs.notification.entity.NotificationStatus;
import com.cfs.notification.entity.NotificationType;

import tools.jackson.databind.json.JsonMapper;

class NotificationResponseSerializationTest {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    void notificationResponsePreservesPropertiesValuesAndNulls() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L, 2L, 3L, null, null, 6L, null,
                NotificationType.EMI_DUE_REMINDER, NotificationChannel.IN_APP, NotificationStatus.SENT,
                "Subject", "Message", LocalDateTime.of(2026, 1, 2, 3, 4, 5), null, null);

        assertEquals(
                "{\"notificationId\":1,\"customerId\":2,\"applicationId\":3,\"approvalId\":null,"
                        + "\"disbursementId\":null,\"repaymentId\":6,\"scheduleId\":null,"
                        + "\"type\":\"EMI_DUE_REMINDER\",\"channel\":\"IN_APP\",\"status\":\"SENT\","
                        + "\"subject\":\"Subject\",\"message\":\"Message\","
                        + "\"createdAt\":\"2026-01-02T03:04:05\",\"sentAt\":null,\"readAt\":null}",
                jsonMapper.writeValueAsString(response));
    }
}

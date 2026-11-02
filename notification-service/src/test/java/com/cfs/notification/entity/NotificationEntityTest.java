package com.cfs.notification.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.PrePersist;
import org.junit.jupiter.api.Test;

class NotificationEntityTest {

    @Test
    void publicNoArgAndExactAccessorSurfaceRetainNullDefaults() throws Exception {
        Notification notification = Notification.class.getConstructor().newInstance();
        assertEquals(1, Notification.class.getConstructors().length);
        assertEquals(0, Notification.class.getConstructors()[0].getParameterCount());
        var fields = Arrays.stream(Notification.class.getDeclaredFields())
                .filter(field -> !field.isSynthetic() && !Modifier.isStatic(field.getModifiers()))
                .toList();
        assertEquals(16, fields.size());
        assertEquals(33, Arrays.stream(Notification.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers())).count());

        for (var field : fields) {
            String suffix = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            var getter = Notification.class.getMethod("get" + suffix);
            var setter = Notification.class.getMethod("set" + suffix, field.getType());
            assertEquals(field.getType(), getter.getReturnType());
            assertEquals(void.class, setter.getReturnType());
            assertNull(getter.invoke(notification), field.getName());
            Object value = sample(field.getType());
            setter.invoke(notification, value);
            assertSame(value, getter.invoke(notification), field.getName());
            setter.invoke(notification, new Object[]{null});
            assertNull(getter.invoke(notification), field.getName());
        }
        for (String method : List.of("equals", "hashCode", "toString")) {
            assertTrue(Arrays.stream(Notification.class.getDeclaredMethods())
                    .noneMatch(m -> m.getName().equals(method)));
        }
    }

    @Test
    void prePersistDefaultsOnlyNullValuesIndependentlyAndIsIdempotent() throws Exception {
        assertTrue(Notification.class.getMethod("prePersist").isAnnotationPresent(PrePersist.class));
        LocalDateTime supplied = LocalDateTime.of(2020, 1, 2, 3, 4);
        for (int mask = 0; mask < 8; mask++) {
            Notification notification = new Notification();
            if ((mask & 1) != 0) notification.setCreatedAt(supplied);
            if ((mask & 2) != 0) notification.setStatus(NotificationStatus.SENT);
            if ((mask & 4) != 0) notification.setChannel(NotificationChannel.EMAIL);
            notification.setSentAt(supplied);
            notification.setReadAt(supplied);
            LocalDateTime before = LocalDateTime.now();
            notification.prePersist();
            LocalDateTime after = LocalDateTime.now();
            if ((mask & 1) != 0) {
                assertSame(supplied, notification.getCreatedAt());
            } else {
                assertFalse(notification.getCreatedAt().isBefore(before));
                assertFalse(notification.getCreatedAt().isAfter(after));
            }
            assertEquals((mask & 2) != 0 ? NotificationStatus.SENT : NotificationStatus.PENDING,
                    notification.getStatus());
            assertEquals((mask & 4) != 0 ? NotificationChannel.EMAIL : NotificationChannel.IN_APP,
                    notification.getChannel());
            LocalDateTime created = notification.getCreatedAt();
            notification.prePersist();
            assertSame(created, notification.getCreatedAt());
            assertSame(supplied, notification.getSentAt());
            assertSame(supplied, notification.getReadAt());
        }
    }

    private Object sample(Class<?> type) {
        if (type == Long.class) return 42L;
        if (type == String.class) return "notification";
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 11, 2, 12, 30);
        if (type.isEnum()) return type.getEnumConstants()[0];
        throw new AssertionError("Unexpected field type: " + type);
    }
}

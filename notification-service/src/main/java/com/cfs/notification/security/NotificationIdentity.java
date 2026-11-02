package com.cfs.notification.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class NotificationIdentity {

    private NotificationIdentity() {
    }

    public static NotificationJwtAuthenticationToken current() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof NotificationJwtAuthenticationToken jwt) || !jwt.isAuthenticated()) {
            throw new AccessDeniedException("Access denied");
        }
        return jwt;
    }

    public static boolean hasRole(NotificationJwtAuthenticationToken jwt, String role) {
        return jwt.getAuthorities().stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    public static void requireAdmin() {
        if (!hasRole(current(), "ROLE_ADMIN")) {
            throw new AccessDeniedException("Access denied");
        }
    }
}

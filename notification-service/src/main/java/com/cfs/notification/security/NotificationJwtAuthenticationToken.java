package com.cfs.notification.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/** Authentication created only after JWT signature and claims validation. */
public final class NotificationJwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final Long userId;
    private final String bearerToken;

    NotificationJwtAuthenticationToken(String subject, Long userId, String bearerToken,
            Collection<? extends GrantedAuthority> authorities) {
        super(subject, null, authorities);
        this.userId = userId;
        this.bearerToken = bearerToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBearerToken() {
        return bearerToken;
    }
}

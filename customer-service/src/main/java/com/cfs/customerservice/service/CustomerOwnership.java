package com.cfs.customerservice.service;

import java.math.BigDecimal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.cfs.customerservice.entity.Customer;

@Component
public class CustomerOwnership {

    public void assertOwned(Customer customer) {
        assertUserId(customer.getUserId());
    }

    public void assertUserId(Long userId) {
        if (!currentUserId().equals(userId)) {
            throw new AccessDeniedException("You can only access your own customer profile");
        }
    }

    public Long currentUserId() {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt)
                || !jwt.isAuthenticated()) {
            throw new AccessDeniedException("Authenticated JWT is required");
        }
        Object claim = jwt.getToken().getClaim("userId");
        try {
            if (claim instanceof Number number) {
                return new BigDecimal(number.toString()).longValueExact();
            }
            if (claim instanceof String text) {
                return Long.valueOf(text.trim());
            }
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new AccessDeniedException("JWT userId must be an integer", exception);
        }
        throw new AccessDeniedException("JWT userId is required");
    }
}

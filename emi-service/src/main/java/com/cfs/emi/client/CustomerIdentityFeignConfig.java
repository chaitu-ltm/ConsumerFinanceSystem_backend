package com.cfs.emi.client;

import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import feign.RequestInterceptor;

// Deliberately not component-scanned: this interceptor belongs only to CustomerServiceClient.
public class CustomerIdentityFeignConfig {
    @Bean
    public RequestInterceptor callerJwtInterceptor() {
        return template -> {
            if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt)
                    || !jwt.isAuthenticated()) {
                throw new AccessDeniedException("Authenticated JWT is required");
            }
            template.removeHeader("Authorization");
            template.header("Authorization", "Bearer " + jwt.getToken().getTokenValue());
        };
    }
}

package com.cfs.disbursement.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

// Registered only in the ApprovalClient child context.
public class FeignConfig {

    @Bean
    public RequestInterceptor authorizationHeaderInterceptor() {

        return template -> {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                    && jwtAuthentication.isAuthenticated()) {

                String tokenValue =
                        jwtAuthentication.getToken().getTokenValue();

                template.header(
                        "Authorization",
                        "Bearer " + tokenValue
                );
            }
        };
    }
}
package com.cfs.notification.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.cfs.notification.security.NotificationIdentity;

import feign.Feign;
import feign.RequestInterceptor;

// Deliberately not component-scanned: these beans belong only to CustomerClient.
public class CustomerClientConfiguration {

    @Bean
    public RequestInterceptor customerBearerToken() {
        return template -> {
            var jwt = NotificationIdentity.current();
            template.header("Authorization");
            template.header("Authorization", "Bearer " + jwt.getBearerToken());
        };
    }

    @Bean
    @Scope("prototype")
    public Feign.Builder customerFeignBuilder() {
        // Keep identity lookup on the invoking thread even if circuit breakers are enabled globally.
        return Feign.builder();
    }
}

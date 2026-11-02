package com.cfs.applicationservice.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

@Configuration
public class CircuitBreakerSecurityConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService securityContextCircuitBreakerExecutor() {
        // Capture each caller's context at submission, restoring/clearing it on pooled workers.
        return new DelegatingSecurityContextExecutorService(Executors.newCachedThreadPool());
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerSecurityContext(
            @Qualifier("securityContextCircuitBreakerExecutor") ExecutorService executor) {
        // Feign's enabled circuit breaker runs on an executor even with its time limiter disabled.
        return factory -> factory.configureExecutorService(executor);
    }
}

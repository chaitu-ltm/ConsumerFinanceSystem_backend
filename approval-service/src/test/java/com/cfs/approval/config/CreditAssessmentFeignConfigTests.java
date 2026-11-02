package com.cfs.approval.config;

import com.cfs.approval.client.CreditAssessmentClient;
import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CreditAssessmentFeignConfigTests {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void actualFeignInvocationForwardsCurrentJwtOnly() {
        AtomicReference<Request> sent = new AtomicReference<>();
        CreditAssessmentClient client = Feign.builder().contract(new SpringMvcContract())
                .requestInterceptor(new CreditAssessmentFeignConfig().authorizationHeaderInterceptor())
                .client((request, options) -> {
                    sent.set(request);
                    return Response.builder().request(request).status(204).headers(Map.of()).build();
                }).target(CreditAssessmentClient.class, "http://credit");
        for (String token : List.of("first-test-token", "second-test-token")) {
            Jwt jwt = Jwt.withTokenValue(token).header("alg", "HS256").subject("manager")
                    .claim("roles", List.of("ROLE_CREDIT_MANAGER")).build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
            client.getById(20L);
            assertEquals("http://credit/api/credit-assessments/20", sent.get().url());
            assertEquals(List.of("Bearer " + token), List.copyOf(sent.get().headers().get("Authorization")));
        }
        SecurityContextHolder.clearContext();
        client.getById(20L);
        assertFalse(sent.get().headers().containsKey("Authorization"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager", "not-a-jwt", List.of()));
        client.getById(20L);
        assertFalse(sent.get().headers().containsKey("Authorization"));
        Jwt jwt = Jwt.withTokenValue("unauthenticated").header("alg", "HS256").subject("manager").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        client.getById(20L);
        assertFalse(sent.get().headers().containsKey("Authorization"));
    }

    @Test
    void configurationIsNotGloballyComponentScanned() {
        assertFalse(CreditAssessmentFeignConfig.class.isAnnotationPresent(Configuration.class));
    }
}

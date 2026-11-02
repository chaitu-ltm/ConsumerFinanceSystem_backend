package com.cfs.emi.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cfs.emi.dto.CustomerIdentityResponse;

import feign.Feign;
import feign.Request;
import feign.Response;

class CustomerServiceClientTest {
    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void existingEndpointContractForwardsOnlyCurrentBearerToCustomerService() {
        AtomicReference<Request> captured = new AtomicReference<>();
        CustomerIdentityResponse identity = new CustomerIdentityResponse();
        identity.setUserId(77L);
        identity.setCustomerId(10L);
        CustomerServiceClient client = Feign.builder().contract(new SpringMvcContract())
                .requestInterceptor(new CustomerIdentityFeignConfig().callerJwtInterceptor())
                .client((request, options) -> {
                    captured.set(request);
                    return Response.builder().request(request).status(200).headers(Map.of()).build();
                }).decoder((response, type) -> identity)
                .target(CustomerServiceClient.class, "http://customer-service");
        authenticate("first-token");
        assertSame(identity, client.getByUserId(77L));
        assertEquals("http://customer-service/api/customers/user/77", captured.get().url());
        assertEquals(Request.HttpMethod.GET, captured.get().httpMethod());
        assertEquals(List.of("Bearer first-token"), List.copyOf(captured.get().headers().get("Authorization")));
        authenticate("second-token");
        client.getByUserId(77L);
        assertEquals(List.of("Bearer second-token"), List.copyOf(captured.get().headers().get("Authorization")));
        assertFalse(CustomerIdentityFeignConfig.class.isAnnotationPresent(Configuration.class));
    }

    private static void authenticate(String token) {
        Jwt jwt = Jwt.withTokenValue(token).header("alg", "HS256").claim("userId", 77L).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
    }
}

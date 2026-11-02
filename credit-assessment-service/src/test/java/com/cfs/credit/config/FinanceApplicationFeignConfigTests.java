package com.cfs.credit.config;

import com.cfs.credit.client.FinanceApplicationClient;
import feign.Feign;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import com.cfs.credit.dto.response.ApplicationAssessmentContextResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class FinanceApplicationFeignConfigTests {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void actualFeignInvocationUsesExactPathAndCurrentStaffTokenOnly() {
        AtomicReference<Request> sent = new AtomicReference<>();
        FinanceApplicationClient client = Feign.builder().contract(new SpringMvcContract())
                .requestInterceptor(new FinanceApplicationFeignConfig().authorizationHeaderInterceptor())
                .client((request, options) -> {
                    sent.set(request);
                    return Response.builder().request(request).status(204).headers(Map.of()).build();
                }).target(FinanceApplicationClient.class, "http://finance");
        for (String token : List.of("first-original-token", "second-original-token")) {
            Jwt jwt = Jwt.withTokenValue(token).header("alg", "HS256").subject("analyst")
                    .claim("roles", List.of("ROLE_CREDIT_ANALYST")).build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
            client.getAssessmentContext(12L);
            assertEquals("http://finance/api/applications/12/assessment-context", sent.get().url());
            assertEquals("GET", sent.get().httpMethod().name());
            assertEquals(List.of("Bearer " + token), List.copyOf(sent.get().headers().get("Authorization")));
        }
        SecurityContextHolder.clearContext();
        client.getAssessmentContext(12L);
        assertFalse(sent.get().headers().containsKey("Authorization"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("analyst", "not-a-jwt", List.of()));
        client.getAssessmentContext(12L);
        assertFalse(sent.get().headers().containsKey("Authorization"));
        Jwt jwt = Jwt.withTokenValue("unauthenticated").header("alg", "HS256").subject("analyst").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        client.getAssessmentContext(12L);
        assertFalse(sent.get().headers().containsKey("Authorization"));
    }

    @Test
    void reusedTemplateDoesNotRetainOldAuthorization() {
        RequestTemplate template = new RequestTemplate();
        template.header("Authorization", "Bearer old-token");
        new FinanceApplicationFeignConfig().authorizationHeaderInterceptor().apply(template);
        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    void configurationIsClientScopedAndTargetsFinanceService() {
        FeignClient annotation = FinanceApplicationClient.class.getAnnotation(FeignClient.class);
        assertEquals("finance-application-service", annotation.name());
        assertArrayEquals(new Class<?>[]{FinanceApplicationFeignConfig.class}, annotation.configuration());
        assertFalse(FinanceApplicationFeignConfig.class.isAnnotationPresent(Configuration.class));
    }

    @Test
    void localContextExposesOnlyContractFields() {
        Set<String> properties = Arrays.stream(BeanUtils.getPropertyDescriptors(
                        ApplicationAssessmentContextResponse.class))
                .map(property -> property.getName()).filter(name -> !"class".equals(name))
                .collect(Collectors.toSet());
        assertEquals(Set.of("applicationId", "status"), properties);
    }

    @Test
    void financeTimeoutsAreBoundedAndClientSpecific() throws Exception {
        Properties properties = new Properties();
        try (var input = new ClassPathResource("application.properties").getInputStream()) {
            properties.load(input);
        }
        String prefix = "spring.cloud.openfeign.client.config.finance-application-service.";
        assertEquals("2000", properties.getProperty(prefix + "connectTimeout"));
        assertEquals("5000", properties.getProperty(prefix + "readTimeout"));
        assertNull(properties.getProperty("spring.cloud.openfeign.client.config.default.connectTimeout"));
        assertNull(properties.getProperty("spring.cloud.openfeign.client.config.default.readTimeout"));
    }
}

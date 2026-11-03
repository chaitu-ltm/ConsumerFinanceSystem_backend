package com.cfs.applicationservice.config;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

class SecurityConfigTests {
    private final byte[] key = new byte[32];
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        new SecureRandom().nextBytes(key);
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "security.jwt.secret-base64=" + Base64.getEncoder().encodeToString(key));
        context.register(WebConfig.class, SecurityConfig.class);
        context.refresh();
        mvc = webAppContextSetup(context).addFilters(context.getBean(FilterChainProxy.class)).build();
    }

    @AfterEach
    void tearDown() {
        context.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsMissingInvalidExpiredWrongKeyAndBasicAuthentication() throws Exception {
        mvc.perform(post("/api/applications")).andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
        mvc.perform(post("/api/applications").header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/applications").header("Authorization",
                "Bearer " + token(key, List.of("ROLE_CUSTOMER"), Instant.now().minusSeconds(120))))
                .andExpect(status().isUnauthorized());
        byte[] wrongKey = new byte[32];
        new SecureRandom().nextBytes(wrongKey);
        mvc.perform(post("/api/applications").header("Authorization",
                "Bearer " + token(wrongKey, List.of("ROLE_CUSTOMER"), Instant.now().plusSeconds(300))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/applications").header("Authorization", "Basic YWRtaW46YWRtaW4xMjM="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerApplicationOperationsRequireCustomerAndRetainPrincipal() throws Exception {
        for (MockHttpServletRequestBuilder request : List.of(post("/api/applications"),
                post("/api/applications/1/submit"), get("/api/applications/1"),
                get("/api/applications/customer/1"), get("/api/applications/1/history"))) {
            mvc.perform(request.header("Authorization", bearer("ROLE_CUSTOMER")))
                    .andExpect(status().isOk()).andExpect(content().string("submitting-customer"));
        }
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_MANAGER", "ROLE_CREDIT_ANALYST")) {
            for (MockHttpServletRequestBuilder request : List.of(post("/api/applications"),
                    post("/api/applications/1/submit"), get("/api/applications/1"),
                    get("/api/applications/customer/1"), get("/api/applications/1/history"))) {
                mvc.perform(request.header("Authorization", bearer(role)))
                        .andExpect(status().isForbidden());
            }
        }
        mvc.perform(post("/api/applications").header("Authorization", bearer("CUSTOMER")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/applications").header("Authorization",
                "Bearer " + token(key, "ROLE_CUSTOMER", Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden());
    }

    @Test
    void assessmentQueueAllowsOnlyStaffAndDoesNotRequireQueryParameter() throws Exception {
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_ANALYST", "ROLE_CREDIT_MANAGER")) {
            for (String path : List.of("/api/applications/assessment-queue",
                    "/api/applications/assessment-queue?status=SUBMITTED")) {
                mvc.perform(get(path).header("Authorization", bearer(role)))
                        .andExpect(status().isOk());
            }
        }
        for (String role : List.of("ROLE_CUSTOMER", "ROLE_LOAN_OFFICER", "ROLE_UNKNOWN",
                "ADMIN", "CREDIT_ANALYST", "CREDIT_MANAGER")) {
            mvc.perform(get("/api/applications/assessment-queue").header("Authorization", bearer(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void assessmentQueueRejectsMissingAndInvalidTokensWithoutRedirect() throws Exception {
        mvc.perform(get("/api/applications/assessment-queue"))
                .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("Location"));
        mvc.perform(get("/api/applications/assessment-queue").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("Location"));
    }

    @Test
    void assessmentQueueDoesNotAuthorizeOtherMethodsOrDescendantPaths() throws Exception {
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_ANALYST", "ROLE_CREDIT_MANAGER")) {
            for (String method : List.of("POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")) {
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method),
                                "/api/applications/assessment-queue").header("Authorization", bearer(role)))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(get("/api/applications/assessment-queue/extra").header("Authorization", bearer(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void assessmentContextAllowsOnlyStaffRoles() throws Exception {
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_ANALYST", "ROLE_CREDIT_MANAGER")) {
            mvc.perform(get("/api/applications/1/assessment-context")
                            .header("Authorization", bearer(role)))
                    .andExpect(status().isOk()).andExpect(content().string("submitting-customer"));
        }
        for (String role : List.of("ROLE_CUSTOMER", "ROLE_UNKNOWN", "ADMIN",
                "CREDIT_ANALYST", "CREDIT_MANAGER")) {
            mvc.perform(get("/api/applications/1/assessment-context")
                            .header("Authorization", bearer(role)))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/applications/1/assessment-context").header("Authorization",
                        "Bearer " + token(key, List.of(), Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden());
    }

    @Test
    void assessmentContextRejectsMissingInvalidAndExpiredTokens() throws Exception {
        String path = "/api/applications/1/assessment-context";
        mvc.perform(get(path)).andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
        mvc.perform(get(path).header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(path).header("Authorization",
                        "Bearer " + token(key, List.of("ROLE_ADMIN"), Instant.now().minusSeconds(120))))
                .andExpect(status().isUnauthorized());
        byte[] wrongKey = new byte[32];
        new SecureRandom().nextBytes(wrongKey);
        mvc.perform(get(path).header("Authorization",
                        "Bearer " + token(wrongKey, List.of("ROLE_ADMIN"), Instant.now().plusSeconds(300))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void assessmentContextDoesNotAuthorizeOtherMethodsOrPaths() throws Exception {
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_ANALYST", "ROLE_CREDIT_MANAGER",
                "ROLE_CUSTOMER")) {
            for (String method : List.of("POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")) {
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method),
                                "/api/applications/1/assessment-context")
                                .header("Authorization", bearer(role)))
                        .andExpect(status().isForbidden());
            }
            mvc.perform(request(org.springframework.http.HttpMethod.TRACE,
                            "/api/applications/1/assessment-context")
                            .header("Authorization", bearer(role)))
                    .andExpect(status().isBadRequest());
            for (String path : List.of("/api/applications/1/assessment-context/extra",
                    "/api/applications/1/extra/assessment-context")) {
                mvc.perform(get(path).header("Authorization", bearer(role)))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void catalogAndHealthRemainPublicButProductCreationIsAdminOnly() throws Exception {
        for (String path : List.of("/api/loan-products", "/api/loan-products/1",
                "/actuator/health", "/actuator/health/readiness", "/actuator/info")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        mvc.perform(post("/api/loan-products")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/loan-products").header("Authorization", bearer("ROLE_ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(post("/api/loan-products").header("Authorization", bearer("ROLE_CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unmatchedRoutesAndMethodsAreDenied() throws Exception {
        for (MockHttpServletRequestBuilder request : List.of(get("/unknown"), get("/api/applications"),
                delete("/api/applications/1"), post("/api/loan-products/1"),
                get("/api/loan-products/1/unknown"))) {
            mvc.perform(request.header("Authorization", bearer("ROLE_ADMIN")))
                    .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Location"));
        }
        mvc.perform(get("/unknown")).andExpect(status().isUnauthorized());
    }

    private String bearer(String role) throws Exception {
        return "Bearer " + token(key, List.of(role), Instant.now().plusSeconds(300));
    }

    private String token(byte[] signingKey, Object roles, Instant expires) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
                .subject("submitting-customer").issueTime(Date.from(Instant.now().minusSeconds(300)))
                .expirationTime(Date.from(expires)).claim("roles", roles).build());
        jwt.sign(new MACSigner(signingKey));
        return jwt.serialize();
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import(Endpoints.class)
    static class WebConfig {}

    @RestController
    static class Endpoints {
        @RequestMapping("/**")
        String endpoint() {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth instanceof JwtAuthenticationToken jwt ? jwt.getName() : "public";
        }
    }
}

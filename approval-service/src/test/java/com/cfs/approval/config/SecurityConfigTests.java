package com.cfs.approval.config;

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
    void rejectsMissingInvalidExpiredWrongKeyAndFormerBasicCredentials() throws Exception {
        mvc.perform(post("/api/approvals")).andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
        mvc.perform(get("/api/approvals").header("Authorization", "Bearer malformed"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/approvals").header("Authorization",
                "Bearer " + token(key, List.of("ROLE_ADMIN"), Instant.now().minusSeconds(120))))
                .andExpect(status().isUnauthorized());
        byte[] wrongKey = new byte[32];
        new SecureRandom().nextBytes(wrongKey);
        mvc.perform(get("/api/approvals").header("Authorization",
                "Bearer " + token(wrongKey, List.of("ROLE_ADMIN"), Instant.now().plusSeconds(300))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/approvals").header("Authorization", "Basic YWRtaW46YWRtaW4xMjM="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAdminAndManagerCanCreateWhileAnalystCanRead() throws Exception {
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_MANAGER")) {
            mvc.perform(post("/api/approvals").header("Authorization", bearer(role)))
                    .andExpect(status().isOk()).andExpect(content().string("reviewer"));
        }
        for (String role : List.of("ROLE_CREDIT_ANALYST", "ROLE_CUSTOMER")) {
            mvc.perform(post("/api/approvals").header("Authorization", bearer(role)))
                    .andExpect(status().isForbidden());
        }
        for (String role : List.of("ROLE_ADMIN", "ROLE_CREDIT_MANAGER", "ROLE_CREDIT_ANALYST")) {
            for (String path : List.of("/api/approvals", "/api/approvals/1", "/api/approvals/application/1",
                    "/api/approvals/status/APPROVED", "/api/approvals/reviewer")) {
                mvc.perform(get(path).header("Authorization", bearer(role)))
                        .andExpect(status().isOk()).andExpect(content().string("reviewer"));
            }
        }
        mvc.perform(get("/api/approvals").header("Authorization", bearer("ROLE_CUSTOMER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/approvals").header("Authorization", bearer("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/approvals").header("Authorization",
                "Bearer " + token(key, "ROLE_ADMIN", Instant.now().plusSeconds(300))))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthAndInfoArePublicAndUnmatchedOperationsAreDenied() throws Exception {
        for (String path : List.of("/actuator/health", "/actuator/health/readiness", "/actuator/info")) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        mvc.perform(get("/unknown").header("Authorization", bearer("ROLE_ADMIN")))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Location"));
        mvc.perform(post("/api/approvals/1").header("Authorization", bearer("ROLE_ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/approvals/1").header("Authorization", bearer("ROLE_ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/unknown")).andExpect(status().isUnauthorized());
    }

    private String bearer(String role) throws Exception {
        return "Bearer " + token(key, List.of(role), Instant.now().plusSeconds(300));
    }

    private String token(byte[] signingKey, Object roles, Instant expires) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
                .subject("reviewer").issueTime(Date.from(Instant.now().minusSeconds(300)))
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

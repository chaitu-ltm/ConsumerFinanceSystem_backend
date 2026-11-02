package com.cfs.customerservice.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.cfs.customerservice.controller.*;
import com.cfs.customerservice.entity.Customer;
import com.cfs.customerservice.exceptions.GlobalExceptionHandler;
import com.cfs.customerservice.repository.*;
import com.cfs.customerservice.service.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;

class CustomerOwnershipSecurityTest {
    private final byte[] key = new byte[32];
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private CustomerRepository customers;
    private AdminKycService admin;

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
        customers = context.getBean(CustomerRepository.class);
        admin = context.getBean(AdminKycService.class);
        Customer customer = new Customer();
        customer.setId(10L);
        customer.setUserId(77L);
        customer.setKycStatus("UNDER_REVIEW");
        when(customers.findById(10L)).thenReturn(Optional.of(customer));
        when(customers.findByIdForUpdate(10L)).thenReturn(Optional.of(customer));
        when(customers.findByUserId(77L)).thenReturn(Optional.of(customer));
        when(customers.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        context.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void signedOwnerLookupWorksButOtherProfilesAndSpoofedUserDenied() throws Exception {
        String owner = bearer(77L, "ROLE_CUSTOMER");
        mvc.perform(get("/api/customers/10").header("Authorization", owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.customerId").value(10));
        mvc.perform(get("/api/customers/user/77").header("Authorization", owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(77))
                .andExpect(jsonPath("$.customerId").value(10));
        mvc.perform(get("/api/customers/10").header("Authorization", bearer(10L, "ROLE_CUSTOMER")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/customers/user/10").header("Authorization", owner))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/customers/999").header("Authorization", owner))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/customers/user/999").header("Authorization", bearer(999L, "ROLE_CUSTOMER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void creationBodyMustMatchSignedUserBeforeAnyWrites() throws Exception {
        String body = "{\"userId\":10,\"firstName\":\"Owner\",\"mobileNumber\":\"9999999999\"}";
        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization", bearer(77L, "ROLE_CUSTOMER")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        verify(customers, never()).existsByUserId(any());
        verify(customers, never()).save(any());
        mvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(body.replace("\"userId\":10", "\"userId\":77"))
                .header("Authorization", bearer(77L, "ROLE_CUSTOMER")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value(77));
    }

    @Test
    void adminKycRoutesAndGenericPatchRemainAllowedCustomerForbidden() throws Exception {
        String adminJwt = bearer(999L, "ROLE_ADMIN");
        String customerJwt = bearer(77L, "ROLE_CUSTOMER");
        for (String path : List.of("/api/admin/kyc", "/api/admin/kyc/pending", "/api/admin/kyc/10")) {
            mvc.perform(get(path).header("Authorization", adminJwt)).andExpect(status().isOk());
            mvc.perform(get(path).header("Authorization", customerJwt)).andExpect(status().isForbidden());
        }
        mvc.perform(put("/api/admin/kyc/10/verify").header("Authorization", adminJwt))
                .andExpect(status().isOk());
        mvc.perform(put("/api/admin/kyc/10/verify").header("Authorization", customerJwt))
                .andExpect(status().isForbidden());
        String reject = "{\"reason\":\"Not valid\"}";
        mvc.perform(put("/api/admin/kyc/10/reject").header("Authorization", adminJwt)
                .contentType(MediaType.APPLICATION_JSON).content(reject)).andExpect(status().isOk());
        mvc.perform(put("/api/admin/kyc/10/reject").header("Authorization", customerJwt)
                .contentType(MediaType.APPLICATION_JSON).content(reject)).andExpect(status().isForbidden());
        for (String decision : List.of("VERIFIED", "REJECTED")) {
            String body = "{\"status\":\"" + decision + "\",\"remarks\":\"Not valid\"}";
            mvc.perform(patch("/api/customers/10/kyc-status").header("Authorization", adminJwt)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
            mvc.perform(patch("/api/customers/10/kyc-status").header("Authorization", customerJwt)
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        }
        verify(admin, times(2)).verifyKyc(10L);
        verify(admin, times(2)).rejectKyc(eq(10L), any());
        mvc.perform(post("/api/customers/10/kyc-review").header("Authorization", adminJwt))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/customers/user/999").header("Authorization", adminJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void unsignedInvalidMissingAndMalformedIdentityNeverTrusted() throws Exception {
        mvc.perform(get("/api/customers/10")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/customers/10").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        for (Object claim : List.of("not-an-id", 77.5)) {
            mvc.perform(get("/api/customers/10").header("Authorization", bearer(claim, "ROLE_CUSTOMER")))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/customers/10").header("Authorization", bearer(null, "ROLE_CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    private String bearer(Object userId, String role) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject("caller")
                .issueTime(Date.from(Instant.now().minusSeconds(30)))
                .expirationTime(Date.from(Instant.now().plusSeconds(300))).claim("roles", List.of(role));
        if (userId != null) claims.claim("userId", userId);
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner(key));
        return "Bearer " + jwt.serialize();
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import({CustomerController.class, AdminKycController.class, GlobalExceptionHandler.class,
            CustomerServiceImpl.class, CustomerOwnership.class})
    static class WebConfig {
        @Bean CustomerRepository customers() { return mock(CustomerRepository.class); }
        @Bean CustomerAddressRepository addresses() { return mock(CustomerAddressRepository.class); }
        @Bean EmploymentDetailRepository employment() { return mock(EmploymentDetailRepository.class); }
        @Bean BankAccountRepository banks() { return mock(BankAccountRepository.class); }
        @Bean CustomerDocumentRepository documents() { return mock(CustomerDocumentRepository.class); }
        @Bean KycReadinessEvaluator evaluator() { return mock(KycReadinessEvaluator.class); }
        @Bean AdminKycService admin() { return mock(AdminKycService.class); }
    }
}

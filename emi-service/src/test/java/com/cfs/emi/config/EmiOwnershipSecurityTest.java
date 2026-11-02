package com.cfs.emi.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
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

import com.cfs.emi.client.*;
import com.cfs.emi.controller.*;
import com.cfs.emi.dto.*;
import com.cfs.emi.entity.*;
import com.cfs.emi.exception.GlobalExceptionHandler;
import com.cfs.emi.repository.*;
import com.cfs.emi.service.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;

import feign.RequestTemplate;

class EmiOwnershipSecurityTest {
    private final byte[] key = new byte[32];
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private CustomerServiceClient customers;
    private LoanAccountRepository loans;
    private EmiInstallmentRepository installments;
    private RepaymentRepository repayments;
    private DisbursementClient disbursements;

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
        customers = context.getBean(CustomerServiceClient.class);
        loans = context.getBean(LoanAccountRepository.class);
        installments = context.getBean(EmiInstallmentRepository.class);
        repayments = context.getBean(RepaymentRepository.class);
        disbursements = context.getBean(DisbursementClient.class);
        LoanAccount account = new LoanAccount();
        account.setLoanAccountId(100L);
        account.setCustomerId(10L);
        account.setApplicationId(500L);
        account.setOutstandingAmount(new BigDecimal("100"));
        account.setStatus(LoanAccountStatus.ACTIVE);
        when(loans.findById(100L)).thenReturn(Optional.of(account));
        when(loans.findByIdForUpdate(100L)).thenReturn(Optional.of(account));
        when(loans.findByApplicationId(500L)).thenReturn(Optional.of(account));
        when(loans.save(any())).thenAnswer(i -> {
            LoanAccount saved = i.getArgument(0);
            saved.setLoanAccountId(100L);
            return saved;
        });
        when(repayments.save(any())).thenAnswer(i -> i.getArgument(0));
        Repayment repayment = new Repayment();
        repayment.setRepaymentId(200L);
        repayment.setLoanAccountId(100L);
        repayment.setEmiId(300L);
        when(repayments.findById(200L)).thenReturn(Optional.of(repayment));
        when(customers.getByUserId(77L)).thenReturn(identity(77L, 10L));
        DisbursementResponse disbursement = new DisbursementResponse();
        disbursement.setDisbursementId(1L);
        disbursement.setApplicationId(500L);
        disbursement.setAmount(new BigDecimal("100.00"));
        disbursement.setStatus("DISBURSED");
        when(disbursements.getById(1L)).thenReturn(disbursement);
        unpaidInstallment();
    }

    @AfterEach
    void tearDown() {
        context.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerReadsAndRepaysWithCallerJwtAvailableAtLookupTime() throws Exception {
        String owner = bearer(77L, "ROLE_CUSTOMER");
        when(customers.getByUserId(77L)).thenAnswer(i -> {
            RequestTemplate template = new RequestTemplate();
            new CustomerIdentityFeignConfig().callerJwtInterceptor().apply(template);
            org.junit.jupiter.api.Assertions.assertEquals(List.of(owner),
                    List.copyOf(template.headers().get("Authorization")));
            return identity(77L, 10L);
        });
        for (String path : reads()) {
            mvc.perform(get(path).header("Authorization", owner)).andExpect(status().isOk());
        }
        mvc.perform(post("/api/emi/repayments").header("Authorization", owner)
                .contentType(MediaType.APPLICATION_JSON).content(payment())).andExpect(status().isCreated());
        verify(customers, times(5)).getByUserId(77L);
        verify(repayments).save(any());
    }

    @Test
    void otherCustomerDeniedOnEveryReadAndRepaymentWithNoWrites() throws Exception {
        when(customers.getByUserId(77L)).thenReturn(identity(77L, 20L));
        String caller = bearer(77L, "ROLE_CUSTOMER");
        for (String path : reads()) {
            mvc.perform(get(path).header("Authorization", caller))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        }
        mvc.perform(post("/api/emi/repayments").header("Authorization", caller)
                .contentType(MediaType.APPLICATION_JSON).content(payment()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        verify(loans, never()).save(any());
        verify(installments, never()).save(any());
        verify(repayments, never()).save(any());
        verify(installments, never()).findById(any());
    }

    @Test
    void staffReadBypassPreservesOfficerCannotRepayAndAdminOnlyGeneration() throws Exception {
        for (String role : List.of("ROLE_ADMIN", "ROLE_LOAN_OFFICER")) {
            String staff = bearer(null, role);
            for (String path : reads()) {
                mvc.perform(get(path).header("Authorization", staff)).andExpect(status().isOk());
            }
        }
        mvc.perform(post("/api/emi/repayments").header("Authorization", bearer(null, "ROLE_LOAN_OFFICER"))
                .contentType(MediaType.APPLICATION_JSON).content(payment())).andExpect(status().isForbidden());
        mvc.perform(post("/api/emi/repayments").header("Authorization", bearer(null, "ROLE_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(payment())).andExpect(status().isCreated());
        String schedule = "{\"applicationId\":500,\"customerId\":10,\"disbursementId\":1,"
                + "\"principalAmount\":100,\"annualInterestRate\":0,\"tenureMonths\":1,\"firstDueDate\":\"2026-12-01\"}";
        for (String role : List.of("ROLE_CUSTOMER", "ROLE_LOAN_OFFICER")) {
            mvc.perform(post("/api/emi/schedules").header("Authorization", bearer(77L, role))
                    .contentType(MediaType.APPLICATION_JSON).content(schedule)).andExpect(status().isForbidden());
        }
        mvc.perform(post("/api/emi/schedules").header("Authorization", bearer(null, "ROLE_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(schedule)).andExpect(status().isCreated());
        verifyNoInteractions(customers);
    }

    @Test
    void mismatchedIdentityAndDependencyUnavailableHaveExplicitSafeStatuses() throws Exception {
        String owner = bearer(77L, "ROLE_CUSTOMER");
        when(customers.getByUserId(77L)).thenReturn(identity(88L, 10L));
        mvc.perform(get("/api/emi/schedules/application/500").header("Authorization", owner))
                .andExpect(status().isForbidden());
        when(customers.getByUserId(77L)).thenThrow(new RuntimeException("private upstream detail"));
        mvc.perform(post("/api/emi/repayments").header("Authorization", owner)
                .contentType(MediaType.APPLICATION_JSON).content(payment()))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Customer ownership verification is temporarily unavailable"));
        verify(repayments, never()).save(any());
        verify(loans, never()).save(any());
    }

    @Test
    void missingResourcesAndInvalidAuthenticationPreserveExpectedStatuses() throws Exception {
        String owner = bearer(77L, "ROLE_CUSTOMER");
        for (String path : List.of("/api/emi/schedules/application/999", "/api/emi/schedules/999/installments",
                "/api/emi/repayments/application/999")) {
            mvc.perform(get(path).header("Authorization", owner)).andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/emi/repayments/999").header("Authorization", owner))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/emi/schedules/application/500")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/emi/schedules/application/500").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/emi/schedules/application/500").header("Authorization", bearer(77L, "ROLE_CREDIT_ANALYST")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/api/emi/schedules/application/500").header("Authorization", bearer(null, "ROLE_CUSTOMER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(customers);
    }

    private void unpaidInstallment() {
        EmiInstallment installment = new EmiInstallment();
        installment.setEmiId(300L);
        installment.setLoanAccountId(100L);
        installment.setEmiAmount(new BigDecimal("100"));
        installment.setAmountPaid(BigDecimal.ZERO);
        installment.setStatus(EmiStatus.PENDING);
        when(installments.findById(300L)).thenReturn(Optional.of(installment));
    }

    private static List<String> reads() {
        return List.of("/api/emi/schedules/application/500", "/api/emi/schedules/100/installments",
                "/api/emi/repayments/application/500", "/api/emi/repayments/200");
    }

    private static String payment() {
        return "{\"loanAccountId\":100,\"emiId\":300,\"amount\":100}";
    }

    private static CustomerIdentityResponse identity(Long userId, Long customerId) {
        CustomerIdentityResponse response = new CustomerIdentityResponse();
        response.setUserId(userId);
        response.setCustomerId(customerId);
        return response;
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
    @Import({EmiScheduleController.class, RepaymentController.class, GlobalExceptionHandler.class,
            EmiScheduleServiceImpl.class, RepaymentServiceImpl.class, LoanAccountOwnership.class})
    static class WebConfig {
        @Bean LoanAccountRepository loans() { return mock(LoanAccountRepository.class); }
        @Bean EmiInstallmentRepository installments() { return mock(EmiInstallmentRepository.class); }
        @Bean RepaymentRepository repayments() { return mock(RepaymentRepository.class); }
        @Bean CustomerServiceClient customers() { return mock(CustomerServiceClient.class); }
        @Bean DisbursementClient disbursements() { return mock(DisbursementClient.class); }
    }
}

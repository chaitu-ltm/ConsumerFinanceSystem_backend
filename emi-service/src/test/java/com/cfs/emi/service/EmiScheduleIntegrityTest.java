package com.cfs.emi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cfs.emi.client.DisbursementFeignConfig;
import com.cfs.emi.client.DisbursementClient;
import com.cfs.emi.dto.DisbursementResponse;
import com.cfs.emi.dto.GenerateEmiScheduleRequest;
import com.cfs.emi.entity.EmiInstallment;
import com.cfs.emi.entity.LoanAccount;
import com.cfs.emi.exception.DisbursementLookupUnavailableException;
import com.cfs.emi.exception.DisbursementNotFoundException;
import com.cfs.emi.exception.DisbursementVerificationException;
import com.cfs.emi.exception.DuplicateEmiScheduleException;
import com.cfs.emi.exception.InvalidEmiScheduleRequestException;
import com.cfs.emi.repository.EmiInstallmentRepository;
import com.cfs.emi.repository.LoanAccountRepository;
import com.cfs.emi.exception.GlobalExceptionHandler;
import feign.RequestTemplate;
import feign.Request;

class EmiScheduleIntegrityTest {
    private final LoanAccountRepository loans = mock(LoanAccountRepository.class);
    private final EmiInstallmentRepository installments = mock(EmiInstallmentRepository.class);
    private final DisbursementClient disbursements = mock(DisbursementClient.class);
    private final LoanAccountOwnership ownership = mock(LoanAccountOwnership.class);
    private final EmiScheduleServiceImpl service =
            new EmiScheduleServiceImpl(loans, installments, ownership, disbursements);
    private final List<EmiInstallment> savedInstallments = new ArrayList<>();

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        savedInstallments.clear();
        when(loans.save(any(LoanAccount.class))).thenAnswer(invocation -> {
            LoanAccount account = invocation.getArgument(0);
            if (account.getLoanAccountId() == null) {
                account.setLoanAccountId(17L);
            }
            return account;
        });
        when(installments.save(any(EmiInstallment.class))).thenAnswer(invocation -> {
            EmiInstallment installment = invocation.getArgument(0);
            savedInstallments.add(installment);
            return installment;
        });
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 50L, "100.00", "DISBURSED"));
    }

    @Test
    void zeroRateThreeInstallmentsReconcileAllMoneyToPrincipal() {
        var response = service.generateSchedule(request("100.00", "0", 3));

        assertEquals(List.of("33.33", "33.33", "33.34"), amounts());
        assertEquals(new BigDecimal("100.00"), response.getTotalPayableAmount());
        assertEquals(new BigDecimal("100.00"), response.getOutstandingAmount());
        assertEquals(new BigDecimal("100.00"), sumPrincipal());
        assertEquals(new BigDecimal("33.33"), response.getMonthlyEmiAmount());
    }

    @Test
    void positiveRateUsesReducingBalanceAndEveryStoredAmountIsReconciled() {
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 50L, "1000.00", "DISBURSED"));
        var response = service.generateSchedule(request("1000.00", "12", 12));

        assertEquals(new BigDecimal("88.85"), response.getMonthlyEmiAmount());
        assertEquals(new BigDecimal("10.00"), savedInstallments.get(0).getInterestComponent());
        assertEquals(new BigDecimal("1000.00"), sumPrincipal());
        assertEquals(response.getTotalPayableAmount(), sumAmounts());
        assertEquals(response.getTotalPayableAmount(), response.getOutstandingAmount());
        assertEquals(BigDecimal.ZERO.setScale(2),
                savedInstallments.get(savedInstallments.size() - 1).getOpeningPrincipal()
                        .subtract(savedInstallments.get(savedInstallments.size() - 1).getPrincipalComponent()));
    }

    @Test
    void repeatingDecimalRateAndTenureRemainScaleTwoAndReconcile() {
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 50L, "1234.56", "DISBURSED"));
        var response = service.generateSchedule(request("1234.56", "5.1234", 7));

        assertMoneyValues();
        assertEquals(new BigDecimal("1234.56"), sumPrincipal());
        assertEquals(sumAmounts(), response.getTotalPayableAmount());
        assertEquals(sumAmounts(), response.getOutstandingAmount());
    }

    @Test
    void tinyLoanCannotCreateNegativePrincipalAndFinalPaymentReconciles() {
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 50L, "0.01", "DISBURSED"));
        var response = service.generateSchedule(request("0.01", "0", 3));

        assertEquals(List.of("0.00", "0.00", "0.01"), amounts());
        assertEquals(new BigDecimal("0.01"), sumPrincipal());
        assertEquals(new BigDecimal("0.01"), response.getTotalPayableAmount());
        assertTrue(savedInstallments.stream().allMatch(item -> item.getPrincipalComponent().signum() >= 0));
        assertEquals(2, savedInstallments.stream()
                .filter(item -> item.getEmiAmount().signum() == 0 && item.getStatus() == com.cfs.emi.entity.EmiStatus.PAID)
                .count());
    }

    @Test
    void rejectsInputPrecisionThatWouldDifferFromPersistedTermsBeforeUpstreamLookup() {
        assertThrows(InvalidEmiScheduleRequestException.class,
                () -> service.generateSchedule(request("1.001", "0", 1)));
        verifyNoInteractions(disbursements);
        assertThrows(InvalidEmiScheduleRequestException.class,
                () -> service.generateSchedule(request("1.00", "1.00001", 1)));
        verify(loans, never()).save(any());
        verifyNoInteractions(installments);
    }

    @Test
    void duplicateScheduleReturnsConflictBeforeRemoteCallOrWrites() {
        when(loans.existsByApplicationId(50L)).thenReturn(true);

        assertThrows(DuplicateEmiScheduleException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        assertEquals(409, new GlobalExceptionHandler()
                .handleDuplicate(new DuplicateEmiScheduleException("duplicate")).getStatusCode().value());
        verifyNoInteractions(disbursements, installments);
        verify(loans, never()).save(any());
    }

    @Test
    void missingOrMismatchedDisbursementStopsBeforeAnyAccountOrInstallmentWrite() {
        when(disbursements.getById(90L)).thenReturn(null);
        assertThrows(DisbursementNotFoundException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        assertEquals(400, new GlobalExceptionHandler().handleDisbursementNotFound(
                new DisbursementNotFoundException()).getStatusCode().value());
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 51L, "100.00", "DISBURSED"));
        assertThrows(DisbursementVerificationException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        when(disbursements.getById(90L)).thenReturn(disbursement(91L, 50L, "100.00", "DISBURSED"));
        assertThrows(DisbursementVerificationException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 50L, "100.01", "DISBURSED"));
        assertThrows(DisbursementVerificationException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        when(disbursements.getById(90L)).thenReturn(disbursement(90L, 50L, "100.00", "PENDING"));
        assertThrows(DisbursementVerificationException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        when(disbursements.getById(90L)).thenAnswer(invocation -> {
            throw feignNotFound();
        });
        assertThrows(DisbursementNotFoundException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));
        assertEquals(409, new GlobalExceptionHandler().handleDisbursementMismatch(
                new DisbursementVerificationException()).getStatusCode().value());
        verify(loans, never()).save(any());
        verifyNoInteractions(installments);
    }

    @Test
    void upstreamFailureIsSanitizedAsServiceUnavailable() {
        when(disbursements.getById(90L)).thenThrow(new IllegalStateException("private upstream detail"));

        DisbursementLookupUnavailableException exception = assertThrows(DisbursementLookupUnavailableException.class,
                () -> service.generateSchedule(request("100.00", "0", 3)));

        var response = new GlobalExceptionHandler().handleDisbursementUnavailable(exception);
        assertEquals(503, response.getStatusCode().value());
        assertEquals("Disbursement verification is temporarily unavailable", response.getBody().get("message"));
        assertFalse(response.getBody().toString().contains("private upstream detail"));
        verify(loans, never()).save(any());
        verifyNoInteractions(installments);
    }

    @Test
    void disbursementFeignInterceptorUsesCurrentCallerJwtAndReplacesExistingHeader() {
        authenticate("first-token");
        RequestTemplate first = new RequestTemplate();
        first.header("Authorization", "machine-token");
        new DisbursementFeignConfig().callerJwtInterceptor().apply(first);
        assertEquals(List.of("Bearer first-token"), List.copyOf(first.headers().get("Authorization")));

        authenticate("second-token");
        RequestTemplate second = new RequestTemplate();
        new DisbursementFeignConfig().callerJwtInterceptor().apply(second);
        assertEquals(List.of("Bearer second-token"), List.copyOf(second.headers().get("Authorization")));
    }

    private static void authenticate(String token) {
        Jwt jwt = Jwt.withTokenValue(token).header("alg", "HS256").subject("admin").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private GenerateEmiScheduleRequest request(String principal, String rate, int months) {
        GenerateEmiScheduleRequest request = new GenerateEmiScheduleRequest();
        request.setApplicationId(50L);
        request.setCustomerId(60L);
        request.setDisbursementId(90L);
        request.setPrincipalAmount(new BigDecimal(principal));
        request.setAnnualInterestRate(new BigDecimal(rate));
        request.setTenureMonths(months);
        request.setFirstDueDate(LocalDate.of(2026, 12, 1));
        return request;
    }

    private static DisbursementResponse disbursement(Long id, Long application, String amount, String status) {
        DisbursementResponse response = new DisbursementResponse();
        response.setDisbursementId(id);
        response.setApplicationId(application);
        response.setAmount(new BigDecimal(amount));
        response.setStatus(status);
        return response;
    }

    private static feign.FeignException feignNotFound() {
        Request request = Request.create(Request.HttpMethod.GET, "http://disbursement-service/api/disbursements/90",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return feign.FeignException.errorStatus("getById", feign.Response.builder().request(request).status(404)
                .headers(Map.of()).build());
    }

    private List<String> amounts() {
        return savedInstallments.stream().map(item -> item.getEmiAmount().toPlainString()).toList();
    }

    private BigDecimal sumPrincipal() {
        return savedInstallments.stream().map(EmiInstallment::getPrincipalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
    }

    private BigDecimal sumAmounts() {
        return savedInstallments.stream().map(EmiInstallment::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
    }

    private void assertMoneyValues() {
        assertTrue(savedInstallments.stream().allMatch(item ->
                item.getPrincipalComponent().scale() == 2 && item.getInterestComponent().scale() == 2
                        && item.getEmiAmount().scale() == 2));
    }
}

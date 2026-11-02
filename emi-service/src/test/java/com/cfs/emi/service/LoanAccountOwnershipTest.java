package com.cfs.emi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cfs.emi.client.*;
import com.cfs.emi.dto.*;
import com.cfs.emi.entity.*;
import com.cfs.emi.exception.*;
import com.cfs.emi.repository.*;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

class LoanAccountOwnershipTest {
    private final LoanAccountRepository loans = mock(LoanAccountRepository.class);
    private final EmiInstallmentRepository installments = mock(EmiInstallmentRepository.class);
    private final RepaymentRepository repayments = mock(RepaymentRepository.class);
    private final CustomerServiceClient customers = mock(CustomerServiceClient.class);
    private final DisbursementClient disbursements = mock(DisbursementClient.class);
    private final LoanAccountOwnership ownership = new LoanAccountOwnership(loans, customers);
    private final EmiScheduleServiceImpl schedules = new EmiScheduleServiceImpl(loans, installments, ownership,
            disbursements);
    private final RepaymentServiceImpl service = new RepaymentServiceImpl(loans, installments, repayments, ownership);
    private LoanAccount account;
    private Repayment repayment;

    @BeforeEach
    void setUp() {
        authenticate(77L, "ROLE_CUSTOMER", "caller-token");
        account = new LoanAccount();
        account.setLoanAccountId(100L);
        account.setApplicationId(500L);
        account.setCustomerId(10L);
        account.setOutstandingAmount(new BigDecimal("100"));
        account.setStatus(LoanAccountStatus.ACTIVE);
        when(loans.findById(100L)).thenReturn(Optional.of(account));
        when(loans.findByIdForUpdate(100L)).thenReturn(Optional.of(account));
        when(loans.findByApplicationId(500L)).thenReturn(Optional.of(account));
        when(customers.getByUserId(77L)).thenReturn(identity(77L, 10L));
        repayment = new Repayment();
        repayment.setRepaymentId(200L);
        repayment.setLoanAccountId(100L);
        repayment.setEmiId(300L);
        when(repayments.findById(200L)).thenReturn(Optional.of(repayment));
        when(repayments.save(any())).thenAnswer(i -> i.getArgument(0));
        EmiInstallment installment = new EmiInstallment();
        installment.setEmiId(300L);
        installment.setLoanAccountId(100L);
        installment.setInstallmentNumber(1);
        installment.setEmiAmount(new BigDecimal("100"));
        installment.setAmountPaid(BigDecimal.ZERO);
        installment.setStatus(EmiStatus.PENDING);
        when(installments.findById(300L)).thenReturn(Optional.of(installment));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    static Stream<String> operations() {
        return Stream.of("schedule", "installments", "repaymentApplication", "repaymentId", "repay");
    }

    private Object invoke(String operation) {
        return switch (operation) {
            case "schedule" -> schedules.getByApplicationId(500L);
            case "installments" -> schedules.getInstallments(100L);
            case "repaymentApplication" -> service.getByApplicationId(500L);
            case "repaymentId" -> service.getById(200L);
            case "repay" -> service.recordRepayment(request(), "owner");
            default -> throw new AssertionError(operation);
        };
    }

    @ParameterizedTest
    @MethodSource("operations")
    void ownAccountAllowedWithDistinctIdsAndSingleLookup(String operation) {
        assertDoesNotThrow(() -> invoke(operation));
        verify(customers, times(1)).getByUserId(77L);
        if (operation.equals("repay")) {
            verify(repayments).save(any());
            verify(installments).save(any());
            verify(loans).save(account);
        }
    }

    @ParameterizedTest
    @MethodSource("operations")
    void otherAccountDeniedBeforeChildQueriesOrWrites(String operation) {
        when(customers.getByUserId(77L)).thenReturn(identity(77L, 77L));
        assertThrows(AccessDeniedException.class, () -> invoke(operation));
        verifyNoInteractions(installments);
        verify(repayments, never()).save(any());
        verify(repayments, never()).findByLoanAccountIdOrderByPaymentDateDesc(any());
        verify(loans, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("operations")
    void missingLinkedLoanRemains404WithoutRemoteLookup(String operation) {
        when(loans.findById(100L)).thenReturn(Optional.empty());
        when(loans.findByIdForUpdate(100L)).thenReturn(Optional.empty());
        when(loans.findByApplicationId(500L)).thenReturn(Optional.empty());
        assertThrows(LoanAccountNotFoundException.class, () -> invoke(operation));
        verifyNoInteractions(customers, installments);
    }

    @Test
    void absentRepaymentPreservesExisting400AndAbsentInstallment404() {
        when(repayments.findById(200L)).thenReturn(Optional.empty());
        assertThrows(InvalidRepaymentException.class, () -> service.getById(200L));
        verifyNoInteractions(customers);
        when(installments.findById(300L)).thenReturn(Optional.empty());
        assertThrows(EmiInstallmentNotFoundException.class, () -> service.recordRepayment(request(), "owner"));
        verify(repayments, never()).save(any());
    }

    static Stream<CustomerIdentityResponse> invalidMappings() {
        return Stream.of(null, identity(null, 10L), identity(88L, 10L), identity(77L, null), identity(77L, 20L));
    }

    @ParameterizedTest
    @MethodSource("invalidMappings")
    void nullOrInconsistentMappingFailsClosed(CustomerIdentityResponse response) {
        when(customers.getByUserId(77L)).thenReturn(response);
        assertThrows(AccessDeniedException.class, () -> service.recordRepayment(request(), "owner"));
        verifyNoInteractions(installments, repayments);
        verify(loans, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404})
    void upstreamIdentityDenialFailsClosed403(int status) {
        when(customers.getByUserId(77L)).thenThrow(feignError(status));
        assertThrows(AccessDeniedException.class, () -> ownership.byId(100L, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 500, 503})
    void upstreamUnavailableIsSanitized503(int status) {
        when(customers.getByUserId(77L)).thenThrow(feignError(status));
        CustomerLookupUnavailableException exception = assertThrows(CustomerLookupUnavailableException.class,
                () -> service.recordRepayment(request(), "owner"));
        var response = new GlobalExceptionHandler().handleCustomerUnavailable(exception);
        assertEquals(503, response.getStatusCode().value());
        assertEquals("Customer ownership verification is temporarily unavailable", response.getBody().get("message"));
        assertFalse(response.getBody().toString().contains("private upstream detail"));
        verifyNoInteractions(installments, repayments);
    }

    @Test
    void timeoutFailsClosedAnd403HandlerDoesNotReturn500() {
        when(customers.getByUserId(77L)).thenThrow(new RuntimeException("private upstream detail"));
        assertThrows(CustomerLookupUnavailableException.class, () -> ownership.byId(100L, true));
        assertEquals(403, new GlobalExceptionHandler().handleAccessDenied(
                new AccessDeniedException("Forbidden")).getStatusCode().value());
    }

    @ParameterizedTest
    @MethodSource("operations")
    void adminSkipsRemoteEvenWithoutUserId(String operation) {
        authenticate(null, "ROLE_ADMIN", "admin-token");
        assertDoesNotThrow(() -> invoke(operation));
        verifyNoInteractions(customers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"schedule", "installments", "repaymentApplication", "repaymentId"})
    void officerReadsSkipRemoteEvenWithoutUserId(String operation) {
        authenticate(null, "ROLE_LOAN_OFFICER", "staff-token");
        assertDoesNotThrow(() -> invoke(operation));
        verifyNoInteractions(customers);
    }

    @Test
    void officerCannotRepayOrOtherRolesBypass() {
        authenticate(77L, "ROLE_LOAN_OFFICER", "staff-token");
        assertThrows(AccessDeniedException.class, () -> service.recordRepayment(request(), "staff"));
        authenticate(77L, "ROLE_CREDIT_ANALYST", "staff-token");
        assertThrows(AccessDeniedException.class, () -> ownership.byId(100L, true));
        verifyNoInteractions(customers);
    }

    static Stream<Object> invalidClaims() {
        return Stream.of("bad", "77.2", new BigDecimal("77.9"), new BigInteger("9223372036854775808"), true);
    }

    @ParameterizedTest
    @MethodSource("invalidClaims")
    void invalidClaimsFailBeforeLookup(Object claim) {
        authenticate(claim, "ROLE_CUSTOMER", "caller-token");
        assertThrows(AccessDeniedException.class, () -> ownership.byId(100L, true));
        verifyNoInteractions(customers);
    }

    @Test
    void missingClaimAndMissingOrNonJwtAuthenticationRejected() {
        authenticate(null, "ROLE_CUSTOMER", "caller-token");
        assertThrows(AccessDeniedException.class, () -> ownership.byId(100L, true));
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> ownership.byId(100L, true));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner", "unused", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThrows(AccessDeniedException.class, () -> ownership.byId(100L, true));
        verifyNoInteractions(customers);
    }

    @Test
    void interceptorUsesInvocationContextReplacesHeaderAndNeverCachesJwt() {
        var interceptor = new CustomerIdentityFeignConfig().callerJwtInterceptor();
        RequestTemplate first = new RequestTemplate();
        first.header("Authorization", "Bearer stale");
        interceptor.apply(first);
        assertEquals(List.of("Bearer caller-token"), List.copyOf(first.headers().get("Authorization")));
        authenticate(88L, "ROLE_CUSTOMER", "second-token");
        RequestTemplate second = new RequestTemplate();
        interceptor.apply(second);
        assertEquals(List.of("Bearer second-token"), List.copyOf(second.headers().get("Authorization")));
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> interceptor.apply(new RequestTemplate()));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "unused"));
        assertThrows(AccessDeniedException.class, () -> interceptor.apply(new RequestTemplate()));
    }

    @Test
    void successiveOperationsResolveIdentityAgainRatherThanCache() {
        schedules.getByApplicationId(500L);
        when(customers.getByUserId(77L)).thenReturn(identity(77L, 20L));
        assertThrows(AccessDeniedException.class, () -> schedules.getInstallments(100L));
        verify(customers, times(2)).getByUserId(77L);
    }

    private static CustomerIdentityResponse identity(Long userId, Long customerId) {
        CustomerIdentityResponse identity = new CustomerIdentityResponse();
        identity.setUserId(userId);
        identity.setCustomerId(customerId);
        return identity;
    }

    private static RepaymentRequest request() {
        RepaymentRequest request = new RepaymentRequest();
        request.setLoanAccountId(100L);
        request.setEmiId(300L);
        request.setAmount(new BigDecimal("100"));
        return request;
    }

    private static FeignException feignError(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://customer-service/api/customers/user/77",
                Map.of(), null, StandardCharsets.UTF_8, null);
        if (status == -1) return new feign.RetryableException(-1, "private upstream detail", Request.HttpMethod.GET,
                new java.net.SocketTimeoutException("private upstream detail"), (Long) null, request);
        return FeignException.errorStatus("getByUserId", feign.Response.builder().request(request).status(status)
                .reason("private upstream detail").headers(Map.of()).build());
    }

    static void authenticate(Object userId, String role, String token) {
        Jwt.Builder builder = Jwt.withTokenValue(token).header("alg", "HS256").subject("owner");
        if (userId != null) builder.claim("userId", userId);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(builder.build(),
                List.of(new SimpleGrantedAuthority(role))));
    }
}

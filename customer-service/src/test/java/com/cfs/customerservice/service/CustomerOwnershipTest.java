package com.cfs.customerservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cfs.customerservice.dto.*;
import com.cfs.customerservice.entity.*;
import com.cfs.customerservice.exceptions.ResourceNotFoundException;
import com.cfs.customerservice.repository.*;

class CustomerOwnershipTest {
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CustomerAddressRepository addresses = mock(CustomerAddressRepository.class);
    private final EmploymentDetailRepository employment = mock(EmploymentDetailRepository.class);
    private final BankAccountRepository banks = mock(BankAccountRepository.class);
    private final CustomerDocumentRepository documents = mock(CustomerDocumentRepository.class);
    private final KycReadinessEvaluator evaluator = mock(KycReadinessEvaluator.class);
    private final AdminKycService admin = mock(AdminKycService.class);
    private final CustomerOwnership ownership = new CustomerOwnership();
    private CustomerServiceImpl service;
    private Customer customer;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customers, addresses, employment, banks, documents, evaluator, admin, ownership);
        customer = new Customer();
        customer.setId(10L);
        customer.setUserId(77L);
        customer.setKycStatus("UNDER_REVIEW");
        when(customers.findById(10L)).thenReturn(Optional.of(customer));
        when(customers.findByIdForUpdate(10L)).thenReturn(Optional.of(customer));
        when(customers.findByUserId(77L)).thenReturn(Optional.of(customer));
        when(customers.save(any())).thenAnswer(i -> i.getArgument(0));
        when(addresses.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employment.save(any())).thenAnswer(i -> i.getArgument(0));
        when(banks.save(any())).thenAnswer(i -> i.getArgument(0));
        when(documents.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employment.findByCustomerId(10L)).thenReturn(Optional.of(new EmploymentDetail()));
        when(evaluator.evaluate(customer)).thenReturn(mock(KycReadinessEvaluator.Readiness.class));
        authenticate(77L, "ROLE_CUSTOMER");
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    static Stream<String> operations() {
        return Stream.of("profile", "addresses", "employment", "banks", "documents", "onboarding",
                "addAddress", "addEmployment", "addBank", "addDocument", "review");
    }

    private void invoke(String operation, Long id) {
        switch (operation) {
            case "profile" -> service.getCustomer(id);
            case "addresses" -> service.getAddresses(id);
            case "employment" -> service.getEmployment(id);
            case "banks" -> service.getBankAccounts(id);
            case "documents" -> service.getDocuments(id);
            case "onboarding" -> service.getOnboardingStatus(id);
            case "review" -> service.requestKycReview(id);
            case "addAddress" -> {
                AddressRequest request = new AddressRequest();
                request.setAddressType("CURRENT");
                request.setAddressLine1("Street");
                request.setCity("City");
                request.setState("State");
                request.setPincode("123456");
                request.setCountry("India");
                service.addAddress(id, request);
            }
            case "addEmployment" -> {
                EmploymentRequest request = new EmploymentRequest();
                request.setEmploymentType("SALARIED");
                service.addEmployment(id, request);
            }
            case "addBank" -> {
                BankAccountRequest request = new BankAccountRequest();
                request.setAccountHolderName("Owner");
                request.setBankName("Bank");
                request.setAccountNumber("123456789");
                request.setIfscCode("BANK0000001");
                request.setAccountType("SAVINGS");
                service.addBankAccount(id, request);
            }
            case "addDocument" -> {
                DocumentRequest request = new DocumentRequest();
                request.setDocumentType("PAN");
                request.setFileName("pan.pdf");
                request.setFilePath("docs");
                service.addDocument(id, request);
            }
            default -> throw new AssertionError(operation);
        }
    }

    @ParameterizedTest
    @MethodSource("operations")
    void ownerAllowedWithDifferentUserAndCustomerIds(String operation) {
        assertDoesNotThrow(() -> invoke(operation, 10L));
    }

    @ParameterizedTest
    @MethodSource("operations")
    void otherCustomerDeniedBeforeChildQueriesOrWrites(String operation) {
        authenticate(10L, "ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> invoke(operation, 10L));
        verifyNoInteractions(addresses, employment, banks, documents, evaluator, admin);
        verify(customers, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("operations")
    void missingCustomerRemains404(String operation) {
        assertThrows(ResourceNotFoundException.class, () -> invoke(operation, 999L));
        verifyNoInteractions(addresses, employment, banks, documents, evaluator, admin);
    }

    @Test
    void userLookupAndCreationUseSignedUserIdBeforeRepositoryAccess() {
        assertEquals(10L, service.getCustomerByUserId(77L).getCustomerId());
        clearInvocations(customers);
        assertThrows(AccessDeniedException.class, () -> service.getCustomerByUserId(10L));
        CustomerRequest request = profile(10L);
        assertThrows(AccessDeniedException.class, () -> service.createCustomer(request));
        verifyNoInteractions(customers);
        request.setUserId(77L);
        assertEquals(77L, service.createCustomer(request).getUserId());
        verify(customers).save(any());
        when(customers.findByUserId(77L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getCustomerByUserId(77L));
    }

    @Test
    void genericAdminDecisionsDoNotUseCustomerOwnership() {
        authenticate(999L, "ROLE_ADMIN");
        KycStatusRequest request = new KycStatusRequest();
        request.setStatus("VERIFIED");
        service.updateKycStatus(10L, request);
        verify(admin).verifyKyc(10L);
        request.setStatus("REJECTED");
        request.setRemarks("Not valid");
        service.updateKycStatus(10L, request);
        verify(admin).rejectKyc(eq(10L), any());
        assertThrows(AccessDeniedException.class, () -> service.requestKycReview(10L));
    }

    static Stream<Object> invalidClaims() {
        return Stream.of("bad", "77.0", new BigDecimal("77.5"), new BigInteger("9223372036854775808"), true);
    }

    @ParameterizedTest
    @MethodSource("invalidClaims")
    void malformedClaimsNeverTruncate(Object claim) {
        authenticate(claim, "ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> ownership.assertUserId(77L));
    }

    @Test
    void missingUnauthenticatedAndNonJwtRejected() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> ownership.assertUserId(77L));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("77", "unused", List.of()));
        assertThrows(AccessDeniedException.class, () -> ownership.assertUserId(77L));
        authenticate(null, "ROLE_CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> ownership.assertUserId(77L));
        JwtAuthenticationToken unauthenticated = new JwtAuthenticationToken(
                Jwt.withTokenValue("token").header("alg", "HS256").claim("userId", 77L).build());
        SecurityContextHolder.getContext().setAuthentication(unauthenticated);
        assertThrows(AccessDeniedException.class, () -> ownership.assertUserId(77L));
    }

    @Test
    void exactIntegralNumericAndStringClaimsAllowed() {
        for (Object claim : List.of("77", 77, 77L, new BigDecimal("77.00"))) {
            authenticate(claim, "ROLE_CUSTOMER");
            assertDoesNotThrow(() -> ownership.assertUserId(77L));
        }
    }

    private static CustomerRequest profile(Long userId) {
        CustomerRequest request = new CustomerRequest();
        request.setUserId(userId);
        request.setFirstName("Owner");
        request.setMobileNumber("9999999999");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        return request;
    }

    private static void authenticate(Object userId, String role) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "HS256").subject("owner");
        if (userId != null) builder.claim("userId", userId);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(builder.build(),
                List.of(new SimpleGrantedAuthority(role))));
    }
}

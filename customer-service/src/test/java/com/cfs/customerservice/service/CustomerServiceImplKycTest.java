package com.cfs.customerservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.cfs.customerservice.dto.CustomerResponse;
import com.cfs.customerservice.dto.DocumentRequest;
import com.cfs.customerservice.dto.KycStatusRequest;
import com.cfs.customerservice.dto.OnboardingStatusResponse;
import com.cfs.customerservice.entity.Customer;
import com.cfs.customerservice.entity.CustomerAddress;
import com.cfs.customerservice.entity.CustomerDocument;
import com.cfs.customerservice.exceptions.KycPrerequisitesMissingException;
import com.cfs.customerservice.exceptions.ResourceNotFoundException;
import com.cfs.customerservice.exceptions.WorkflowConflictException;
import com.cfs.customerservice.repository.BankAccountRepository;
import com.cfs.customerservice.repository.CustomerAddressRepository;
import com.cfs.customerservice.repository.CustomerDocumentRepository;
import com.cfs.customerservice.repository.CustomerRepository;
import com.cfs.customerservice.repository.EmploymentDetailRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceImplKycTest {

    private static final Long CUSTOMER_ID = 10L;
    private static final Long OWNER_USER_ID = 77L;

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerAddressRepository addressRepository;
    @Mock
    private EmploymentDetailRepository employmentRepository;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private CustomerDocumentRepository documentRepository;
    @Mock
    private AdminKycService adminKycService;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        KycReadinessEvaluator evaluator = new KycReadinessEvaluator(
                addressRepository, employmentRepository, bankAccountRepository, documentRepository);
        service = new CustomerServiceImpl(customerRepository, addressRepository, employmentRepository,
                bankAccountRepository, documentRepository, evaluator, adminKycService, new CustomerOwnership());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        authenticateAs(OWNER_USER_ID);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ---------------- ownership ----------------

    @Test
    void requestKycReview_tokenUserIdMatchingCustomerIdButNotOwner_isForbidden() {
        stubCustomer(customer("PENDING"));
        authenticateAs(CUSTOMER_ID);

        assertThrows(AccessDeniedException.class, () -> service.requestKycReview(CUSTOMER_ID));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void requestKycReview_withoutJwtAuthentication_isForbidden() {
        SecurityContextHolder.clearContext();
        stubCustomer(customer("PENDING"));

        assertThrows(AccessDeniedException.class, () -> service.requestKycReview(CUSTOMER_ID));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void requestKycReview_usesPessimisticLockAndReturns404WhenMissing() {
        when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.empty());
        authenticateAs(OWNER_USER_ID);

        assertThrows(ResourceNotFoundException.class, () -> service.requestKycReview(CUSTOMER_ID));

        verify(customerRepository, never()).findById(anyLong());
    }

    // ---------------- readiness ----------------

    @Test
    void requestKycReview_missingPrerequisites_reportsSectionsAndDocumentTypes() {
        stubCustomer(customer("PENDING"));
        authenticateAs(OWNER_USER_ID);
        when(addressRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of());
        when(employmentRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);
        when(bankAccountRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);
        when(documentRepository.findByCustomerId(CUSTOMER_ID))
                .thenReturn(List.of(document("PAN"), document("SALARY_SLIP")));

        KycPrerequisitesMissingException exception = assertThrows(KycPrerequisitesMissingException.class,
                () -> service.requestKycReview(CUSTOMER_ID));

        assertEquals(List.of("ADDRESS", "BANK_ACCOUNT", "DOCUMENTS"), exception.getMissingSections());
        assertEquals(List.of("IDENTITY_PROOF", "ADDRESS_PROOF", "BANK_STATEMENT"),
                exception.getMissingDocumentTypes());
        assertTrue(exception.getMessage().contains("BANK_STATEMENT"));
        verify(customerRepository, never()).save(any());
    }

    // ---------------- transitions ----------------

    @Test
    void requestKycReview_pendingAndReady_movesToUnderReview() {
        Customer customer = customer("PENDING");
        stubCustomer(customer);
        authenticateAs(OWNER_USER_ID);
        stubAllPrerequisites();

        CustomerResponse response = service.requestKycReview(CUSTOMER_ID);

        assertEquals("UNDER_REVIEW", response.getKycStatus());
        assertEquals("IN_PROGRESS", response.getOnboardingStatus());
        verify(customerRepository).save(customer);
    }

    @Test
    void requestKycReview_rejectedAndReady_isResubmitted() {
        Customer customer = customer("REJECTED");
        customer.setOnboardingStatus("ACTION_REQUIRED");
        stubCustomer(customer);
        authenticateAs(OWNER_USER_ID);
        stubAllPrerequisites();

        CustomerResponse response = service.requestKycReview(CUSTOMER_ID);

        assertEquals("UNDER_REVIEW", response.getKycStatus());
        assertEquals("IN_PROGRESS", response.getOnboardingStatus());
        verify(customerRepository).save(customer);
    }

    @Test
    void requestKycReview_alreadyUnderReview_returnsCurrentWithoutSaving() {
        Customer customer = customer("UNDER_REVIEW");
        customer.setOnboardingStatus("IN_PROGRESS");
        stubCustomer(customer);
        authenticateAs(OWNER_USER_ID);

        CustomerResponse response = service.requestKycReview(CUSTOMER_ID);

        assertEquals("UNDER_REVIEW", response.getKycStatus());
        assertEquals("IN_PROGRESS", response.getOnboardingStatus());
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(documentRepository, addressRepository, bankAccountRepository, employmentRepository);
    }

    @Test
    void requestKycReview_verified_isConflict() {
        stubCustomer(customer("VERIFIED"));
        authenticateAs(OWNER_USER_ID);

        assertThrows(WorkflowConflictException.class, () -> service.requestKycReview(CUSTOMER_ID));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void requestKycReview_unknownState_isConflict() {
        stubCustomer(customer("SUSPENDED"));
        authenticateAs(OWNER_USER_ID);

        assertThrows(WorkflowConflictException.class, () -> service.requestKycReview(CUSTOMER_ID));

        verify(customerRepository, never()).save(any());
    }

    // ---------------- generic admin PATCH ----------------

    @Test
    void updateKycStatus_verified_delegatesToAdminDecision() {
        stubCustomer(customer("UNDER_REVIEW"));

        service.updateKycStatus(CUSTOMER_ID, statusRequest("verified", null));

        verify(customerRepository).findByIdForUpdate(CUSTOMER_ID);
        verify(adminKycService).verifyKyc(CUSTOMER_ID);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateKycStatus_rejected_delegatesWithRemarksAsReason() {
        stubCustomer(customer("UNDER_REVIEW"));

        service.updateKycStatus(CUSTOMER_ID, statusRequest("REJECTED", "Blurry PAN"));

        verify(adminKycService).rejectKyc(eq(CUSTOMER_ID),
                argThat(request -> "Blurry PAN".equals(request.getReason())));
    }

    @Test
    void updateKycStatus_adminSubmitOrReset_isConflict() {
        stubCustomer(customer("REJECTED"));

        assertThrows(WorkflowConflictException.class,
                () -> service.updateKycStatus(CUSTOMER_ID, statusRequest("UNDER_REVIEW", null)));
        assertThrows(WorkflowConflictException.class,
                () -> service.updateKycStatus(CUSTOMER_ID, statusRequest("PENDING", null)));

        verifyNoInteractions(adminKycService);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateKycStatus_invalidStatus_isConflict() {
        stubCustomer(customer("UNDER_REVIEW"));

        assertThrows(WorkflowConflictException.class,
                () -> service.updateKycStatus(CUSTOMER_ID, statusRequest("DONE", null)));

        verifyNoInteractions(adminKycService);
    }

    // ---------------- onboarding ----------------

    @Test
    void getOnboardingStatus_requiresAllFiveDocumentTypes() {
        Customer customer = customer("VERIFIED");
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        stubAllPrerequisites();
        when(documentRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(document("PAN"),
                document("IDENTITY_PROOF"), document("ADDRESS_PROOF"), document("SALARY_SLIP")));

        OnboardingStatusResponse response = service.getOnboardingStatus(CUSTOMER_ID);

        assertFalse(response.isDocumentsCompleted());
        assertEquals("IN_PROGRESS", response.getOnboardingStatus());
    }

    @Test
    void getOnboardingStatus_verifiedAndComplete_isCompleted() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer("VERIFIED")));
        stubAllPrerequisites();

        OnboardingStatusResponse response = service.getOnboardingStatus(CUSTOMER_ID);

        assertTrue(response.isDocumentsCompleted());
        assertEquals("VERIFIED", response.getKycStatus());
        assertEquals("COMPLETED", response.getOnboardingStatus());
    }

    @Test
    void getOnboardingStatus_rejectedAndComplete_isActionRequired() {
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer("REJECTED")));
        stubAllPrerequisites();

        assertEquals("ACTION_REQUIRED", service.getOnboardingStatus(CUSTOMER_ID).getOnboardingStatus());
    }

    @Test
    void addDocument_forRejectedCustomer_keepsActionRequired() {
        Customer customer = customer("REJECTED");
        customer.setOnboardingStatus("ACTION_REQUIRED");
        stubCustomer(customer);
        stubAllPrerequisites();
        when(documentRepository.save(any(CustomerDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        DocumentRequest request = new DocumentRequest();
        request.setDocumentType("salary_slip");
        request.setFileName("slip.pdf");
        request.setFilePath("/docs/slip.pdf");

        service.addDocument(CUSTOMER_ID, request);

        assertEquals("ACTION_REQUIRED", customer.getOnboardingStatus());
        assertEquals("REJECTED", customer.getKycStatus());
    }

    // ---------------- helpers ----------------

    private Customer customer(String kycStatus) {
        Customer customer = new Customer();
        customer.setId(CUSTOMER_ID);
        customer.setUserId(OWNER_USER_ID);
        customer.setCustomerNumber("CFS1");
        customer.setFirstName("Asha");
        customer.setMobileNumber("9999999999");
        customer.setKycStatus(kycStatus);
        customer.setOnboardingStatus("IN_PROGRESS");
        return customer;
    }

    private void stubCustomer(Customer customer) {
        when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    }

    private void stubAllPrerequisites() {
        when(addressRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(new CustomerAddress()));
        when(employmentRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);
        when(bankAccountRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);
        List<CustomerDocument> documents = new ArrayList<>();
        for (String type : KycReadinessEvaluator.REQUIRED_DOCUMENT_TYPES) {
            documents.add(document(type));
        }
        when(documentRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(documents);
    }

    private static CustomerDocument document(String type) {
        CustomerDocument document = new CustomerDocument();
        document.setCustomerId(CUSTOMER_ID);
        document.setDocumentType(type);
        document.setVerificationStatus("UPLOADED");
        document.setUploadedAt(LocalDateTime.now());
        return document;
    }

    private static KycStatusRequest statusRequest(String status, String remarks) {
        KycStatusRequest request = new KycStatusRequest();
        request.setStatus(status);
        request.setRemarks(remarks);
        return request;
    }

    private static void authenticateAs(Long userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .claim("userId", userId)
                .claim("roles", List.of("ROLE_CUSTOMER"))
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")), jwt.getSubject());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertSame(authentication, SecurityContextHolder.getContext().getAuthentication());
    }
}

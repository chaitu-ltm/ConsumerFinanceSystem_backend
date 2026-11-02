package com.cfs.customerservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.cfs.customerservice.dto.AdminKycResponse;
import com.cfs.customerservice.dto.KycActionResponse;
import com.cfs.customerservice.dto.KycRejectRequest;
import com.cfs.customerservice.entity.Customer;
import com.cfs.customerservice.entity.CustomerAddress;
import com.cfs.customerservice.entity.CustomerDocument;
import com.cfs.customerservice.exceptions.KycPrerequisitesMissingException;
import com.cfs.customerservice.exceptions.WorkflowConflictException;
import com.cfs.customerservice.repository.BankAccountRepository;
import com.cfs.customerservice.repository.CustomerAddressRepository;
import com.cfs.customerservice.repository.CustomerDocumentRepository;
import com.cfs.customerservice.repository.CustomerRepository;
import com.cfs.customerservice.repository.EmploymentDetailRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminKycServiceImplTest {

    private static final Long CUSTOMER_ID = 5L;

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

    private AdminKycServiceImpl service;

    @BeforeEach
    void setUp() {
        KycReadinessEvaluator evaluator = new KycReadinessEvaluator(
                addressRepository, employmentRepository, bankAccountRepository, documentRepository);
        service = new AdminKycServiceImpl(customerRepository, documentRepository, evaluator);
    }

    @Test
    void getPendingKyc_queriesUnderReviewQueue() {
        Customer customer = customer("UNDER_REVIEW");
        when(customerRepository.findByKycStatus("UNDER_REVIEW")).thenReturn(List.of(customer));

        List<AdminKycResponse> responses = service.getPendingKyc();

        assertEquals(1, responses.size());
        assertEquals("UNDER_REVIEW", responses.get(0).getKycStatus());
        verify(customerRepository, never()).findByKycStatus("PENDING");
    }

    @Test
    void verifyKyc_underReviewAndReady_verifiesDocumentsAndCompletesOnboarding() {
        Customer customer = customer("UNDER_REVIEW");
        when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        List<CustomerDocument> documents = stubAllPrerequisites();

        KycActionResponse response = service.verifyKyc(CUSTOMER_ID);

        assertEquals("VERIFIED", response.getKycStatus());
        assertEquals("COMPLETED", response.getOnboardingStatus());
        assertTrue(documents.stream().allMatch(d -> "VERIFIED".equals(d.getVerificationStatus())));
        verify(documentRepository).saveAll(documents);
        verify(customerRepository).save(customer);
        verify(customerRepository, never()).findById(any());
    }

    @Test
    void verifyKyc_notUnderReview_isConflict() {
        for (String status : List.of("PENDING", "REJECTED", "VERIFIED")) {
            when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer(status)));
            stubAllPrerequisites();

            assertThrows(WorkflowConflictException.class, () -> service.verifyKyc(CUSTOMER_ID), status);
        }
        verify(customerRepository, never()).save(any());
        verify(documentRepository, never()).saveAll(anyList());
    }

    @Test
    void verifyKyc_underReviewButIncomplete_isBadRequest() {
        when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer("UNDER_REVIEW")));
        stubAllPrerequisites();
        when(bankAccountRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(false);

        KycPrerequisitesMissingException exception = assertThrows(KycPrerequisitesMissingException.class,
                () -> service.verifyKyc(CUSTOMER_ID));

        assertEquals(List.of("BANK_ACCOUNT"), exception.getMissingSections());
        verify(customerRepository, never()).save(any());
        verify(documentRepository, never()).saveAll(anyList());
    }

    @Test
    void rejectKyc_underReview_setsRejectedAndActionRequired() {
        Customer customer = customer("UNDER_REVIEW");
        when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        stubAllPrerequisites();

        KycActionResponse response = service.rejectKyc(CUSTOMER_ID, rejectRequest("PAN unreadable"));

        assertEquals("REJECTED", response.getKycStatus());
        assertEquals("ACTION_REQUIRED", response.getOnboardingStatus());
        assertEquals("PAN unreadable", response.getReason());
        verify(customerRepository).save(customer);
    }

    @Test
    void rejectKyc_blankReason_isBadRequest() {
        when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer("UNDER_REVIEW")));

        assertThrows(IllegalArgumentException.class, () -> service.rejectKyc(CUSTOMER_ID, rejectRequest("  ")));
        assertThrows(IllegalArgumentException.class, () -> service.rejectKyc(CUSTOMER_ID, null));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void rejectKyc_notUnderReview_isConflict() {
        for (String status : List.of("PENDING", "REJECTED", "VERIFIED")) {
            when(customerRepository.findByIdForUpdate(CUSTOMER_ID)).thenReturn(Optional.of(customer(status)));

            assertThrows(WorkflowConflictException.class,
                    () -> service.rejectKyc(CUSTOMER_ID, rejectRequest("reason")), status);
        }
        verify(customerRepository, never()).save(any());
    }

    private Customer customer(String kycStatus) {
        Customer customer = new Customer();
        customer.setId(CUSTOMER_ID);
        customer.setUserId(50L);
        customer.setKycStatus(kycStatus);
        customer.setOnboardingStatus("IN_PROGRESS");
        return customer;
    }

    private List<CustomerDocument> stubAllPrerequisites() {
        when(addressRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(new CustomerAddress()));
        when(employmentRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);
        when(bankAccountRepository.existsByCustomerId(CUSTOMER_ID)).thenReturn(true);
        List<CustomerDocument> documents = new ArrayList<>();
        for (String type : KycReadinessEvaluator.REQUIRED_DOCUMENT_TYPES) {
            CustomerDocument document = new CustomerDocument();
            document.setCustomerId(CUSTOMER_ID);
            document.setDocumentType(type);
            document.setVerificationStatus("UPLOADED");
            documents.add(document);
        }
        when(documentRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(documents);
        when(documentRepository.existsByCustomerIdAndDocumentType(any(), anyString())).thenReturn(true);
        return documents;
    }

    private static KycRejectRequest rejectRequest(String reason) {
        KycRejectRequest request = new KycRejectRequest();
        request.setReason(reason);
        return request;
    }
}

package com.cfs.applicationservice.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.cfs.applicationservice.client.CustomerServiceClient;
import com.cfs.applicationservice.entity.ApplicationStatus;
import com.cfs.applicationservice.entity.FinanceApplication;
import com.cfs.applicationservice.entity.LoanProduct;
import com.cfs.applicationservice.exception.GlobalExceptionHandler;
import com.cfs.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.cfs.applicationservice.repository.FinanceApplicationRepository;
import com.cfs.applicationservice.repository.LoanProductRepository;
import com.cfs.applicationservice.service.FinanceApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AssessmentQueueControllerTests {
    private final FinanceApplicationRepository applications = mock(FinanceApplicationRepository.class);
    private final LoanProductRepository products = mock(LoanProductRepository.class);
    private final ApplicationStatusHistoryRepository history = mock(ApplicationStatusHistoryRepository.class);
    private final CustomerServiceClient customers = mock(CustomerServiceClient.class);
    private final FinanceApplicationServiceImpl service =
            new FinanceApplicationServiceImpl(applications, products, history, customers);
    private final MockMvc mvc = standaloneSetup(new FinanceApplicationController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    private FinanceApplication submitted;

    @BeforeEach
    void setUp() {
        submitted = spy(application(42L, ApplicationStatus.SUBMITTED));
        clearInvocations(submitted);
        when(applications.findByStatus(ApplicationStatus.SUBMITTED)).thenReturn(List.of(submitted));
        LoanProduct product = new LoanProduct();
        product.setId(7L);
        product.setProductName("Personal Loan");
        when(products.findAllById(List.of(7L))).thenReturn(List.of(product));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "?status=SUBMITTED"})
    void returnsExactMinimalContractWithDefaultOrExplicitSubmittedStatus(String query) throws Exception {
        mvc.perform(get("/api/applications/assessment-queue" + query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]", aMapWithSize(10)))
                .andExpect(jsonPath("$[0].applicationId").value(42))
                .andExpect(jsonPath("$[0].applicationNumber").value("APP-42"))
                .andExpect(jsonPath("$[0].customerId").value(100))
                .andExpect(jsonPath("$[0].productId").value(7))
                .andExpect(jsonPath("$[0].productName").value("Personal Loan"))
                .andExpect(jsonPath("$[0].requestedAmount").value(12000.50))
                .andExpect(jsonPath("$[0].requestedTenureMonths").value(12))
                .andExpect(jsonPath("$[0].loanPurpose").value("Education"))
                .andExpect(jsonPath("$[0].submittedAt").value("2026-11-03T10:15:00"))
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));

        assertEquals(ApplicationStatus.SUBMITTED, submitted.getStatus());
        verify(applications).findByStatus(ApplicationStatus.SUBMITTED);
        verify(products).findAllById(List.of(7L));
        verifyNoMoreInteractions(applications, products);
        verifyNoInteractions(history, customers);
        assertTrue(mockingDetails(submitted).getInvocations().stream()
                .noneMatch(invocation -> invocation.getMethod().getName().startsWith("set")));
    }

    @Test
    void draftIsExcludedByStatusSpecificRepositoryQuery() throws Exception {
        FinanceApplication draft = application(43L, ApplicationStatus.DRAFT);
        when(applications.findByStatus(ApplicationStatus.DRAFT)).thenReturn(List.of(draft));

        mvc.perform(get("/api/applications/assessment-queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].applicationId").value(42));

        verify(applications).findByStatus(ApplicationStatus.SUBMITTED);
        verifyNoMoreInteractions(applications);
        verifyNoInteractions(history, customers);
    }

    @Test
    void emptyQueueReturnsEmptyArrayWithoutProductOrHistoryLookup() throws Exception {
        when(applications.findByStatus(ApplicationStatus.SUBMITTED)).thenReturn(List.of());

        mvc.perform(get("/api/applications/assessment-queue"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));

        verify(applications).findByStatus(ApplicationStatus.SUBMITTED);
        verifyNoMoreInteractions(applications);
        verifyNoInteractions(products, history, customers);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "APPROVED", "REJECTED", "CANCELLED", "UNDER_REVIEW",
            "CREDIT_ASSESSMENT_PENDING", "PENDING_APPROVAL", "UNKNOWN", "submitted", " ", ""})
    void unsupportedStatusReturnsExistingBadRequestStructureWithoutAnyRepositoryAccess(String status)
            throws Exception {
        mvc.perform(get("/api/applications/assessment-queue").param("status", status))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Only SUBMITTED status is supported for the assessment queue"));
        verifyNoInteractions(applications, products, history, customers);
    }

    @Test
    void productNamesAreMatchedByIdAndSharedProductsFetchedOnlyOnce() {
        FinanceApplication second = application(44L, ApplicationStatus.SUBMITTED);
        second.setProductId(8L);
        FinanceApplication third = application(45L, ApplicationStatus.SUBMITTED);
        when(applications.findByStatus(ApplicationStatus.SUBMITTED)).thenReturn(List.of(submitted, second, third));
        LoanProduct personal = new LoanProduct();
        personal.setId(7L);
        personal.setProductName("Personal Loan");
        LoanProduct vehicle = new LoanProduct();
        vehicle.setId(8L);
        vehicle.setProductName("Vehicle Loan");
        when(products.findAllById(List.of(7L, 8L))).thenReturn(List.of(vehicle, personal));

        var result = service.getAssessmentQueue(null);

        assertEquals(List.of("Personal Loan", "Vehicle Loan", "Personal Loan"),
                result.stream().map(item -> item.productName()).toList());
        verify(applications).findByStatus(ApplicationStatus.SUBMITTED);
        verify(products).findAllById(List.of(7L, 8L));
        verifyNoMoreInteractions(applications, products);
        verifyNoInteractions(history, customers);
    }

    @Test
    void missingProductPreservesExistingNotFoundErrorWithoutWrites() throws Exception {
        when(products.findAllById(List.of(7L))).thenReturn(List.of());

        mvc.perform(get("/api/applications/assessment-queue"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Loan product not found with ID: 7"));

        verify(applications).findByStatus(ApplicationStatus.SUBMITTED);
        verify(products).findAllById(List.of(7L));
        verifyNoMoreInteractions(applications, products);
        verifyNoInteractions(history, customers);
    }

    @Test
    void assessmentContextStillReadsOnlyIdAndStatusWithoutProductsOrHistory() throws Exception {
        when(applications.findById(42L)).thenReturn(Optional.of(submitted));
        mvc.perform(get("/api/applications/42/assessment-context"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"applicationId\":42,\"status\":\"SUBMITTED\"}"));
        verify(applications).findById(42L);
        verifyNoMoreInteractions(applications);
        verifyNoInteractions(products, history, customers);
    }

    @Test
    void queueOperationOverridesClassTransactionAsReadOnly() throws Exception {
        Transactional transaction = FinanceApplicationServiceImpl.class
                .getMethod("getAssessmentQueue", String.class).getAnnotation(Transactional.class);
        assertNotNull(transaction);
        assertTrue(transaction.readOnly());
    }

    private static FinanceApplication application(Long id, ApplicationStatus status) {
        FinanceApplication application = new FinanceApplication();
        application.setId(id);
        application.setApplicationNumber("APP-" + id);
        application.setCustomerId(100L);
        application.setProductId(7L);
        application.setRequestedAmount(new BigDecimal("12000.50"));
        application.setRequestedTenureMonths(12);
        application.setLoanPurpose("Education");
        application.setSubmittedAt(LocalDateTime.of(2026, 11, 3, 10, 15));
        application.setStatus(status);
        return application;
    }
}

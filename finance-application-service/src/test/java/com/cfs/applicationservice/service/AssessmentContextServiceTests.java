package com.cfs.applicationservice.service;

import java.util.Optional;

import com.cfs.applicationservice.client.CustomerServiceClient;
import com.cfs.applicationservice.dto.ApplicationAssessmentContextResponse;
import com.cfs.applicationservice.entity.ApplicationStatus;
import com.cfs.applicationservice.entity.FinanceApplication;
import com.cfs.applicationservice.exception.ResourceNotFoundException;
import com.cfs.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.cfs.applicationservice.repository.FinanceApplicationRepository;
import com.cfs.applicationservice.repository.LoanProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssessmentContextServiceTests {
    private final FinanceApplicationRepository applications = mock(FinanceApplicationRepository.class);
    private final LoanProductRepository products = mock(LoanProductRepository.class);
    private final ApplicationStatusHistoryRepository history = mock(ApplicationStatusHistoryRepository.class);
    private final CustomerServiceClient customers = mock(CustomerServiceClient.class);
    private final FinanceApplicationService service =
            new FinanceApplicationServiceImpl(applications, products, history, customers);

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void readsEveryStatusWithoutProductLookupWritesHistoryOrCustomerCalls(ApplicationStatus status) {
        FinanceApplication application = spy(new FinanceApplication());
        application.setId(42L);
        application.setProductId(999L);
        application.setStatus(status);
        clearInvocations(application);
        when(applications.findById(42L)).thenReturn(Optional.of(application));

        ApplicationAssessmentContextResponse response = service.getAssessmentContext(42L);

        assertEquals(42L, response.getApplicationId());
        assertEquals(status.name(), response.getStatus());
        verify(applications).findById(42L);
        verify(application).getId();
        verify(application).getStatus();
        verifyNoMoreInteractions(applications, application);
        verifyNoInteractions(products, history, customers);
    }

    @Test
    void missingApplicationUsesExistingNotFoundExceptionWithoutOtherAccess() {
        when(applications.findById(42L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.getAssessmentContext(42L));

        assertEquals("Loan application not found with ID: 42", exception.getMessage());
        verify(applications).findById(42L);
        verifyNoMoreInteractions(applications);
        verifyNoInteractions(products, history, customers);
    }

    @Test
    void methodOverridesClassTransactionAsReadOnly() throws Exception {
        Transactional transaction = FinanceApplicationServiceImpl.class
                .getMethod("getAssessmentContext", Long.class).getAnnotation(Transactional.class);
        assertNotNull(transaction);
        assertTrue(transaction.readOnly());
    }
}

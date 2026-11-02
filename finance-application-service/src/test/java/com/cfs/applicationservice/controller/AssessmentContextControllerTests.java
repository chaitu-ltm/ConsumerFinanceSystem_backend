package com.cfs.applicationservice.controller;

import com.cfs.applicationservice.dto.ApplicationAssessmentContextResponse;
import com.cfs.applicationservice.entity.ApplicationStatus;
import com.cfs.applicationservice.exception.GlobalExceptionHandler;
import com.cfs.applicationservice.exception.ResourceNotFoundException;
import com.cfs.applicationservice.service.FinanceApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AssessmentContextControllerTests {
    private final FinanceApplicationService service = mock(FinanceApplicationService.class);
    private final MockMvc mvc = standaloneSetup(new FinanceApplicationController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void returnsExactlyApplicationIdAndStatusForEveryStatus(ApplicationStatus applicationStatus) throws Exception {
        when(service.getAssessmentContext(42L))
                .thenReturn(new ApplicationAssessmentContextResponse(42L, applicationStatus.name()));

        mvc.perform(get("/api/applications/42/assessment-context"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(content().json("{\"applicationId\":42,\"status\":\"" + applicationStatus.name() + "\"}"));

        verify(service).getAssessmentContext(42L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void missingApplicationReturnsExisting404Response() throws Exception {
        when(service.getAssessmentContext(42L))
                .thenThrow(new ResourceNotFoundException("Loan application not found with ID: 42"));

        mvc.perform(get("/api/applications/42/assessment-context"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Loan application not found with ID: 42"));

        verify(service).getAssessmentContext(42L);
        verifyNoMoreInteractions(service);
    }

    @Test
    void responseIsAnOrdinaryPojoWithOnlyTheTwoContractFields() throws Exception {
        Class<ApplicationAssessmentContextResponse> type = ApplicationAssessmentContextResponse.class;
        assertFalse(type.isRecord());
        assertEquals(2, type.getDeclaredFields().length);
        assertEquals(Long.class, type.getDeclaredField("applicationId").getType());
        assertEquals(String.class, type.getDeclaredField("status").getType());
        ApplicationAssessmentContextResponse empty = type.getConstructor().newInstance();
        assertNull(empty.getApplicationId());
        assertNull(empty.getStatus());
    }
}

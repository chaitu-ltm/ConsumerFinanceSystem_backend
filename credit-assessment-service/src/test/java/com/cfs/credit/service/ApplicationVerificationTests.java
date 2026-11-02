package com.cfs.credit.service;

import com.cfs.credit.client.FinanceApplicationClient;
import com.cfs.credit.controller.CreditAssessmentController;
import com.cfs.credit.dto.request.CreditAssessmentRequest;
import com.cfs.credit.dto.response.ApplicationAssessmentContextResponse;
import com.cfs.credit.entity.AssessmentStatus;
import com.cfs.credit.entity.CreditAssessment;
import com.cfs.credit.exception.ApplicationVerificationException;
import com.cfs.credit.exception.DuplicateAssessmentException;
import com.cfs.credit.exception.GlobalExceptionHandler;
import com.cfs.credit.mapper.CreditAssessmentMapper;
import com.cfs.credit.repository.CreditAssessmentRepository;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.DecodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ApplicationVerificationTests {
    private CreditAssessmentRepository repository;
    private FinanceApplicationClient client;
    private CreditAssessmentMapper mapper;
    private CreditAssessmentServiceImpl service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(CreditAssessmentRepository.class);
        client = mock(FinanceApplicationClient.class);
        mapper = spy(new CreditAssessmentMapper());
        service = new CreditAssessmentServiceImpl(repository, mapper, client);
        mvc = standaloneSetup(new CreditAssessmentController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void submittedAllowsCreationAfterVerificationAndPreservesPrincipal() throws Exception {
        when(client.getAssessmentContext(12L)).thenReturn(context(12L, "SUBMITTED"));
        when(repository.save(any(CreditAssessment.class))).thenAnswer(call -> call.getArgument(0));
        mvc.perform(post("/api/credit-assessments").principal(() -> "authenticated-analyst")
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assessedBy").value("authenticated-analyst"))
                .andExpect(jsonPath("$.applicationId").value(12));
        var order = inOrder(repository, client, mapper);
        order.verify(repository).existsByApplicationId(12L);
        order.verify(client).getAssessmentContext(12L);
        order.verify(mapper).toEntity(any(), eq("authenticated-analyst"));
        ArgumentCaptor<CreditAssessment> saved = ArgumentCaptor.forClass(CreditAssessment.class);
        order.verify(repository).save(saved.capture());
        assertEquals("authenticated-analyst", saved.getValue().getAssessedBy());
        assertEquals(AssessmentStatus.ELIGIBLE, saved.getValue().getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "UNDER_REVIEW", "CREDIT_ASSESSMENT_PENDING", "PENDING_APPROVAL",
            "APPROVED", "REJECTED", "CANCELLED", "submitted", " SUBMITTED", "", "UNKNOWN"})
    void everyNonSubmittedStatusConflicts(String status) throws Exception {
        when(client.getAssessmentContext(12L)).thenReturn(context(12L, status));
        expectFailure(409, "Only a SUBMITTED application can be assessed");
    }

    @Test
    void duplicateIsCheckedBeforeFinance() throws Exception {
        when(repository.existsByApplicationId(12L)).thenReturn(true);
        expectFailure(409, "Credit assessment already exists for application ID: 12");
        verifyNoInteractions(client);
        assertThrows(DuplicateAssessmentException.class, () -> service.createAssessment(request(), "analyst"));
    }

    @ParameterizedTest
    @CsvSource({"404,404", "401,401", "403,403", "500,503", "502,503", "503,503", "504,503", "400,502"})
    void upstreamHttpFailuresAreSanitized(int upstream, int expected) throws Exception {
        when(client.getAssessmentContext(12L)).thenThrow(FeignException.errorStatus("private-method",
                Response.builder().request(feignRequest()).status(upstream).headers(Map.of())
                        .body("PRIVATE BODY JWT STACK TRACE", StandardCharsets.UTF_8).build()));
        expectFailure(expected, message(expected));
    }

    @Test
    void timeoutIsUnavailable() throws Exception {
        when(client.getAssessmentContext(12L)).thenThrow(new RetryableException(
                -1, "PRIVATE timeout", Request.HttpMethod.GET, new SocketTimeoutException("PRIVATE"),
                (Long) null, feignRequest()));
        expectFailure(503, message(503));
    }

    @Test
    void connectionFailureIsUnavailable() throws Exception {
        when(client.getAssessmentContext(12L)).thenThrow(new RetryableException(
                -1, "PRIVATE connection refused", Request.HttpMethod.GET, (Long) null, feignRequest()));
        expectFailure(503, message(503));
    }

    @Test
    void noServiceInstanceIsUnavailable() throws Exception {
        when(client.getAssessmentContext(12L)).thenThrow(FeignException.errorStatus("getAssessmentContext",
                Response.builder().request(feignRequest()).status(503).headers(Map.of())
                        .body("Load balancer does not contain an instance", StandardCharsets.UTF_8).build()));
        expectFailure(503, message(503));
    }

    @Test
    void malformedResponseIsBadGateway() throws Exception {
        when(client.getAssessmentContext(12L)).thenThrow(
                new DecodeException(200, "PRIVATE invalid JSON", feignRequest()));
        expectFailure(502, message(502));
    }

    @Test
    void nullContextIsBadGateway() throws Exception {
        expectFailure(502, message(502));
    }

    @Test
    void nullIdIsBadGateway() throws Exception {
        when(client.getAssessmentContext(12L)).thenReturn(context(null, "SUBMITTED"));
        expectFailure(502, message(502));
    }

    @Test
    void mismatchedIdIsBadGateway() throws Exception {
        when(client.getAssessmentContext(12L)).thenReturn(context(13L, "SUBMITTED"));
        expectFailure(502, message(502));
    }

    @Test
    void missingStatusIsBadGateway() throws Exception {
        when(client.getAssessmentContext(12L)).thenReturn(context(12L, null));
        expectFailure(502, message(502));
        assertThrows(ApplicationVerificationException.class, () -> service.createAssessment(request(), "analyst"));
    }

    private void expectFailure(int status, String message) throws Exception {
        mvc.perform(post("/api/credit-assessments").principal(() -> "analyst")
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.*").value(org.hamcrest.Matchers.hasSize(4)));
        verify(repository, never()).save(any());
        verify(mapper, never()).toEntity(any(), any());
    }

    private String message(int status) {
        return switch (status) {
            case 404 -> "Finance application was not found";
            case 401 -> "Application verification requires valid authentication";
            case 403 -> "Application verification is not permitted";
            case 503 -> "Application verification is temporarily unavailable";
            default -> "Application verification returned an invalid response";
        };
    }

    private static ApplicationAssessmentContextResponse context(Long id, String status) {
        var context = new ApplicationAssessmentContextResponse();
        context.setApplicationId(id);
        context.setStatus(status);
        return context;
    }

    private static Request feignRequest() {
        return Request.create(Request.HttpMethod.GET, "http://finance/api/applications/12/assessment-context",
                Map.of(), null, StandardCharsets.UTF_8, null);
    }

    private static String body() {
        return "{\"applicationId\":12,\"status\":\"ELIGIBLE\",\"reason\":\"Verified credit\"}";
    }

    private static CreditAssessmentRequest request() {
        var request = new CreditAssessmentRequest();
        request.setApplicationId(12L);
        request.setStatus(AssessmentStatus.ELIGIBLE);
        request.setReason("Verified credit");
        return request;
    }
}

package com.cfs.approval.service;

import com.cfs.approval.client.CreditAssessmentClient;
import com.cfs.approval.dto.request.ApprovalDecisionRequest;
import com.cfs.approval.dto.response.CreditAssessmentResponse;
import com.cfs.approval.dto.response.CreditAssessmentResponse.AssessmentStatus;
import com.cfs.approval.entity.ApprovalDecision;
import com.cfs.approval.entity.ApprovalStatus;
import com.cfs.approval.exception.CreditAssessmentUnavailableException;
import com.cfs.approval.exception.DuplicateApprovalException;
import com.cfs.approval.exception.InvalidAssessmentException;
import com.cfs.approval.mapper.ApprovalMapper;
import com.cfs.approval.repository.ApprovalRepository;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.DecodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApprovalServiceImplTests {
    private final ApprovalRepository repository = mock(ApprovalRepository.class);
    private final CreditAssessmentClient client = mock(CreditAssessmentClient.class);
    private final ApprovalServiceImpl service =
            new ApprovalServiceImpl(repository, new ApprovalMapper(), client);
    private ApprovalDecisionRequest request;
    private CreditAssessmentResponse assessment;

    @BeforeEach
    void setUp() {
        request = new ApprovalDecisionRequest();
        request.setApplicationId(10L);
        request.setCreditAssessmentId(20L);
        request.setDecision(ApprovalStatus.APPROVED);
        request.setReviewedBy("manager");
        assessment = new CreditAssessmentResponse();
        assessment.setCreditAssessmentId(20L);
        assessment.setApplicationId(10L);
        assessment.setStatus(AssessmentStatus.ELIGIBLE);
        when(client.getById(20L)).thenReturn(assessment);
    }

    @Test
    void eligibleMatchingAssessmentIsApproved() {
        assessment = JsonMapper.builder().build().readValue(
                "{\"creditAssessmentId\":20,\"applicationId\":10,\"status\":\"ELIGIBLE\"}",
                CreditAssessmentResponse.class);
        when(client.getById(20L)).thenReturn(assessment);
        when(repository.save(any(ApprovalDecision.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.createDecision(request);
        assertEquals(ApprovalStatus.APPROVED, response.decision());
        assertEquals(10L, response.applicationId());
        assertEquals(20L, response.creditAssessmentId());
        verify(client).getById(20L);
        verify(repository).save(any(ApprovalDecision.class));
    }

    @Test
    void rejectedDecisionStillChecksLinkButDoesNotRequireEligibility() {
        request.setDecision(ApprovalStatus.REJECTED);
        when(repository.save(any(ApprovalDecision.class))).thenAnswer(invocation -> invocation.getArgument(0));
        for (AssessmentStatus status : new AssessmentStatus[]{AssessmentStatus.NOT_ELIGIBLE, null}) {
            assessment.setStatus(status);
            assertEquals(ApprovalStatus.REJECTED, service.createDecision(request).decision());
        }
        verify(client, times(2)).getById(20L);
    }

    @Test
    void rejectsNullOrWrongAssessmentIdBeforeSaving() {
        when(client.getById(20L)).thenReturn(null);
        assertInvalid("assessment ID");
        when(client.getById(20L)).thenReturn(assessment);
        assessment.setCreditAssessmentId(null);
        assertInvalid("assessment ID");
        assessment.setCreditAssessmentId(21L);
        assertInvalid("assessment ID");
    }

    @Test
    void bothDecisionsRejectMissingOrMismatchedApplication() {
        for (ApprovalStatus decision : ApprovalStatus.values()) {
            request.setDecision(decision);
            assessment.setApplicationId(null);
            assertInvalid("application ID");
            assessment.setApplicationId(11L);
            assertInvalid("application ID");
        }
    }

    @Test
    void approvalRequiresEligibleStatus() {
        assessment.setStatus(AssessmentStatus.NOT_ELIGIBLE);
        assertInvalid("ELIGIBLE");
        assessment.setStatus(null);
        assertInvalid("ELIGIBLE");
    }

    @Test
    void upstream404IsBusinessFailureForBothDecisions() {
        when(client.getById(20L)).thenThrow(upstream(404));
        for (ApprovalStatus decision : ApprovalStatus.values()) {
            request.setDecision(decision);
            assertInvalid("not found");
        }
    }

    @Test
    void upstreamServerAndAuthorizationFailuresAreSanitizedAndDoNotSave() {
        for (int status : new int[]{400, 401, 403, 500, 503}) {
            doThrow(upstream(status)).when(client).getById(20L);
            var ex = assertThrows(CreditAssessmentUnavailableException.class,
                    () -> service.createDecision(request));
            assertEquals("Unable to verify credit assessment; please try again later", ex.getMessage());
        }
        verify(repository, never()).save(any());
    }

    @Test
    void duplicatePrecheckAvoidsUpstreamCall() {
        when(repository.existsByApplicationId(10L)).thenReturn(true);
        assertThrows(DuplicateApprovalException.class, () -> service.createDecision(request));
        verifyNoInteractions(client);
        verify(repository, never()).save(any());
    }

    @Test
    void transportTimeoutAndDecodeFailureAreSanitizedAndDoNotSave() {
        Request httpRequest = Request.create(Request.HttpMethod.GET, "http://credit/20",
                Map.of(), null, StandardCharsets.UTF_8, null);
        for (FeignException failure : new FeignException[]{
                new RetryableException(-1, "private timeout detail", Request.HttpMethod.GET,
                        (Long) null, httpRequest),
                new DecodeException(200, "private malformed response detail", httpRequest)}) {
            doThrow(failure).when(client).getById(20L);
            var ex = assertThrows(CreditAssessmentUnavailableException.class,
                    () -> service.createDecision(request));
            assertSame(failure, ex.getCause());
            assertEquals("Unable to verify credit assessment; please try again later", ex.getMessage());
        }
        verify(repository, never()).save(any());
    }

    @Test
    void unrelatedProgrammingFailuresAreNotClassifiedAsUpstreamUnavailable() {
        var failure = new IllegalStateException("local bug");
        when(client.getById(20L)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.createDecision(request)));
        verify(repository, never()).save(any());
    }

    private void assertInvalid(String message) {
        var ex = assertThrows(InvalidAssessmentException.class, () -> service.createDecision(request));
        assertTrue(ex.getMessage().contains(message));
        verify(repository, never()).save(any());
    }

    private FeignException upstream(int status) {
        Request httpRequest = Request.create(Request.HttpMethod.GET, "http://credit/20",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("getById", Response.builder().request(httpRequest)
                .status(status).headers(Map.of()).body("private upstream detail", StandardCharsets.UTF_8).build());
    }
}

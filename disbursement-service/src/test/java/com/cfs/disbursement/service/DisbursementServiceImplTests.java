package com.cfs.disbursement.service;

import com.cfs.disbursement.client.ApprovalClient;
import com.cfs.disbursement.dto.request.DisbursementRequest;
import com.cfs.disbursement.dto.response.ApprovalResponse;
import com.cfs.disbursement.entity.Disbursement;
import com.cfs.disbursement.entity.DisbursementStatus;
import com.cfs.disbursement.exception.ApprovalUnavailableException;
import com.cfs.disbursement.exception.DuplicateDisbursementException;
import com.cfs.disbursement.exception.LoanNotApprovedException;
import com.cfs.disbursement.mapper.DisbursementMapper;
import com.cfs.disbursement.repository.DisbursementRepository;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.codec.DecodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DisbursementServiceImplTests {
    private final DisbursementRepository repository = mock(DisbursementRepository.class);
    private final ApprovalClient client = mock(ApprovalClient.class);
    private final DisbursementServiceImpl service =
            new DisbursementServiceImpl(repository, new DisbursementMapper(), client);
    private DisbursementRequest request;
    private ApprovalResponse approval;

    @BeforeEach
    void setUp() {
        request = new DisbursementRequest();
        request.setApplicationId(10L);
        request.setAmount(new BigDecimal("100.00"));
        approval = new ApprovalResponse();
        approval.setApprovalId(30L);
        approval.setApplicationId(10L);
        approval.setDecision("APPROVED");
        when(client.getApprovalByApplicationId(10L)).thenReturn(approval);
    }

    @Test
    void actualDecisionJsonDisbursesAndPreservesAmountAndProcessingFields() {
        approval = JsonMapper.builder().build().readValue(
                "{\"approvalId\":30,\"applicationId\":10,\"creditAssessmentId\":20,\"decision\":\"APPROVED\","
                        + "\"reviewedBy\":\"manager\",\"remarks\":\"ok\"}", ApprovalResponse.class);
        when(client.getApprovalByApplicationId(10L)).thenReturn(approval);
        when(repository.save(any(Disbursement.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.createDisbursement(request, "manager");
        assertEquals(30L, response.getApprovalId());
        assertEquals(10L, response.getApplicationId());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(DisbursementStatus.DISBURSED, response.getStatus());
        assertEquals("manager", response.getProcessedBy());
        assertTrue(response.getTransactionReference().matches("CFSDIS-[A-F0-9]{16}"));
        assertNotNull(response.getDisbursedAt());
    }

    @Test
    void nullResponseAndMissingOrInvalidApprovalIdNeverSave() {
        when(client.getApprovalByApplicationId(10L)).thenReturn(null);
        assertInvalid("approval ID");
        when(client.getApprovalByApplicationId(10L)).thenReturn(approval);
        for (Long id : new Long[]{null, 0L, -1L}) {
            approval.setApprovalId(id);
            assertInvalid("approval ID");
        }
    }

    @Test
    void missingOrWrongApplicationIdNeverSaves() {
        approval.setApplicationId(null);
        assertInvalid("does not match");
        approval.setApplicationId(11L);
        assertInvalid("does not match");
    }

    @Test
    void onlyExactApprovedDecisionIsAccepted() {
        for (String decision : new String[]{null, "REJECTED", "approved", "UNKNOWN", ""}) {
            approval.setDecision(decision);
            assertInvalid("not approved");
        }
    }

    @Test
    void upstream404IsBusinessFailure() {
        when(client.getApprovalByApplicationId(10L)).thenThrow(upstream(404));
        assertInvalid("not found");
    }

    @Test
    void upstreamServerAndAuthorizationFailuresAreSanitizedAndDoNotSave() {
        for (int status : new int[]{400, 401, 403, 500, 503}) {
            doThrow(upstream(status)).when(client).getApprovalByApplicationId(10L);
            var ex = assertThrows(ApprovalUnavailableException.class,
                    () -> service.createDisbursement(request, "manager"));
            assertEquals("Unable to verify approval decision; please try again later", ex.getMessage());
        }
        verify(repository, never()).save(any());
    }

    @Test
    void duplicatePrecheckAvoidsUpstreamCall() {
        when(repository.existsByApplicationId(10L)).thenReturn(true);
        assertThrows(DuplicateDisbursementException.class, () -> service.createDisbursement(request, "manager"));
        verifyNoInteractions(client);
        verify(repository, never()).save(any());
    }

    @Test
    void transportTimeoutAndDecodeFailureAreSanitizedAndDoNotSave() {
        Request httpRequest = Request.create(Request.HttpMethod.GET, "http://approval/application/10",
                Map.of(), null, StandardCharsets.UTF_8, null);
        for (FeignException failure : new FeignException[]{
                new RetryableException(-1, "private timeout detail", Request.HttpMethod.GET,
                        (Long) null, httpRequest),
                new DecodeException(200, "private malformed response detail", httpRequest)}) {
            doThrow(failure).when(client).getApprovalByApplicationId(10L);
            var ex = assertThrows(ApprovalUnavailableException.class,
                    () -> service.createDisbursement(request, "manager"));
            assertSame(failure, ex.getCause());
            assertEquals("Unable to verify approval decision; please try again later", ex.getMessage());
        }
        verify(repository, never()).save(any());
    }

    @Test
    void unrelatedProgrammingFailuresAreNotClassifiedAsUpstreamUnavailable() {
        var failure = new IllegalStateException("local bug");
        when(client.getApprovalByApplicationId(10L)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> service.createDisbursement(request, "manager")));
        verify(repository, never()).save(any());
    }

    private void assertInvalid(String message) {
        var ex = assertThrows(LoanNotApprovedException.class, () -> service.createDisbursement(request, "manager"));
        assertTrue(ex.getMessage().contains(message));
        verify(repository, never()).save(any());
    }

    private FeignException upstream(int status) {
        Request httpRequest = Request.create(Request.HttpMethod.GET, "http://approval/application/10",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("getApproval", Response.builder().request(httpRequest)
                .status(status).headers(Map.of()).body("private upstream detail", StandardCharsets.UTF_8).build());
    }
}

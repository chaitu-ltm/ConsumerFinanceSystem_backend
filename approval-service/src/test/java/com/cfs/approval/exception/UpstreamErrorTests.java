package com.cfs.approval.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class UpstreamErrorTests {
    private final GlobalExceptionHandler advice = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/approvals");

    @Test
    void unavailableIs503WithoutCauseDetails() {
        var response = advice.handleCreditAssessmentUnavailable(
                new CreditAssessmentUnavailableException(new RuntimeException("private upstream detail")), request);
        assertEquals(503, response.getStatusCode().value());
        assertEquals("Unable to verify credit assessment; please try again later", response.getBody().getMessage());
    }

    @Test
    void invalidRelationshipIs400AndDuplicateIs409WithUsefulMessages() {
        var invalid = advice.handleInvalidAssessment(new InvalidAssessmentException("Assessment does not match"), request);
        assertEquals(400, invalid.getStatusCode().value());
        assertEquals("Assessment does not match", invalid.getBody().getMessage());
        assertEquals(409, advice.handleDuplicate(new DuplicateApprovalException("Already exists"), request)
                .getStatusCode().value());
    }

    @Test
    void unexpectedFailureDoesNotExposeRawMessage() {
        var response = advice.handleGeneralException(new RuntimeException("private internal detail"), request);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}

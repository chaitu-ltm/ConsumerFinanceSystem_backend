package com.cfs.disbursement.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class UpstreamErrorTests {
    private final GlobalExceptionHandler advice = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/disbursements");

    @Test
    void unavailableIs503WithoutCauseDetails() {
        var response = advice.handleApprovalUnavailable(
                new ApprovalUnavailableException(new RuntimeException("private upstream detail")), request);
        assertEquals(503, response.getStatusCode().value());
        assertEquals("Unable to verify approval decision; please try again later", response.getBody().get("message"));
    }

    @Test
    void invalidRelationshipIs400AndDuplicateIs409WithUsefulMessages() {
        var invalid = advice.handleLoanNotApproved(new LoanNotApprovedException("Approval does not match"), request);
        assertEquals(400, invalid.getStatusCode().value());
        assertEquals("Approval does not match", invalid.getBody().get("message"));
        assertEquals(409, advice.handleDuplicateDisbursement(new DuplicateDisbursementException("Already exists"), request)
                .getStatusCode().value());
    }
}

package com.cfs.customerservice.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class GlobalExceptionHandlerKycTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void accessDenied_mapsTo403() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDenied(new AccessDeniedException("not yours"));

        assertEquals(403, response.getStatusCode().value());
        assertEquals("not yours", response.getBody().get("message"));
    }

    @Test
    void workflowConflict_mapsTo409() {
        assertEquals(409, handler.handleWorkflowConflict(new WorkflowConflictException("x"))
                .getStatusCode().value());
    }

    @Test
    void missingPrerequisites_mapsTo400WithDetails() {
        ResponseEntity<Map<String, Object>> response = handler.handleKycPrerequisitesMissing(
                new KycPrerequisitesMissingException(List.of("ADDRESS"), List.of("PAN")));

        assertEquals(400, response.getStatusCode().value());
        assertEquals(List.of("ADDRESS"), response.getBody().get("missingSections"));
        assertEquals(List.of("PAN"), response.getBody().get("missingDocumentTypes"));
    }
}

package com.cfs.approval.dto.response;

import com.cfs.approval.entity.ApprovalDecision;
import com.cfs.approval.entity.ApprovalStatus;
import com.cfs.approval.mapper.ApprovalMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalDecisionResponseJsonTests {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void mappedResponsePreservesExactJsonPropertiesAndValues() {
        var entity = new ApprovalDecision();
        entity.setApprovalId(1L);
        entity.setApplicationId(2L);
        entity.setCreditAssessmentId(3L);
        entity.setDecision(ApprovalStatus.APPROVED);
        entity.setReviewedBy("manager");
        entity.setRemarks("approved remarks");
        entity.setDecisionDate(LocalDateTime.of(2026, 11, 2, 12, 30, 45));
        var response = new ApprovalMapper().toResponse(entity);
        var json = mapper.readTree(mapper.writeValueAsString(response));

        assertTrue(ApprovalDecisionResponse.class.isRecord());
        assertEquals(Set.of("approvalId", "applicationId", "creditAssessmentId", "decision",
                "reviewedBy", "remarks", "decisionDate"), json.propertyNames());
        assertEquals(1L, json.get("approvalId").longValue());
        assertEquals(2L, json.get("applicationId").longValue());
        assertEquals(3L, json.get("creditAssessmentId").longValue());
        assertEquals("APPROVED", json.get("decision").stringValue());
        assertEquals("manager", json.get("reviewedBy").stringValue());
        assertEquals("approved remarks", json.get("remarks").stringValue());
        assertEquals("2026-11-02T12:30:45", json.get("decisionDate").stringValue());
        assertEquals(response, mapper.readValue(mapper.writeValueAsString(response),
                ApprovalDecisionResponse.class));
    }

    @Test
    void nullableFieldsRemainNullAndPresent() {
        var response = new ApprovalDecisionResponse(null, null, null, null, null, null, null);
        var json = mapper.readTree(mapper.writeValueAsString(response));
        assertEquals(7, json.size());
        json.forEach(value -> assertTrue(value.isNull()));
    }
}

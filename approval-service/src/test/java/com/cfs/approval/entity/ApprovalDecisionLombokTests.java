package com.cfs.approval.entity;

import jakarta.persistence.PrePersist;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalDecisionLombokTests {
    @Test
    void retainsPublicNoArgConstructionAndExactAccessorSurface() throws Exception {
        assertTrue(Modifier.isPublic(ApprovalDecision.class.getConstructor().getModifiers()));
        var entity = new ApprovalDecision();
        var date = LocalDateTime.of(2026, 11, 2, 12, 0);
        entity.setApprovalId(1L);
        entity.setApplicationId(2L);
        entity.setCreditAssessmentId(3L);
        entity.setDecision(ApprovalStatus.APPROVED);
        entity.setReviewedBy("manager");
        entity.setRemarks("remarks");
        entity.setDecisionDate(date);
        assertEquals(1L, entity.getApprovalId());
        assertEquals(2L, entity.getApplicationId());
        assertEquals(3L, entity.getCreditAssessmentId());
        assertEquals(ApprovalStatus.APPROVED, entity.getDecision());
        assertEquals("manager", entity.getReviewedBy());
        assertEquals("remarks", entity.getRemarks());
        assertSame(date, entity.getDecisionDate());
        Set<String> methods = Arrays.stream(ApprovalDecision.class.getDeclaredMethods())
                .map(method -> method.getName()).collect(Collectors.toSet());
        assertEquals(Set.of("getApprovalId", "setApprovalId", "getApplicationId", "setApplicationId",
                "getCreditAssessmentId", "setCreditAssessmentId", "getDecision", "setDecision",
                "getReviewedBy", "setReviewedBy", "getRemarks", "setRemarks",
                "getDecisionDate", "setDecisionDate", "prePersist"), methods);
    }

    @Test
    void prePersistOnlyDefaultsMissingDecisionDate() throws Exception {
        assertTrue(ApprovalDecision.class.getMethod("prePersist").isAnnotationPresent(PrePersist.class));
        var entity = new ApprovalDecision();
        assertNull(entity.getDecisionDate());
        entity.prePersist();
        assertNotNull(entity.getDecisionDate());
        var supplied = LocalDateTime.of(2020, 1, 2, 3, 4);
        entity.setDecisionDate(supplied);
        entity.prePersist();
        assertSame(supplied, entity.getDecisionDate());
    }
}

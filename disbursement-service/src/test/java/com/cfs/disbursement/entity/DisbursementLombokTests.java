package com.cfs.disbursement.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DisbursementLombokTests {
    @Test
    void retainsPublicNoArgConstructionAndExactAccessorSurface() throws Exception {
        assertTrue(Modifier.isPublic(Disbursement.class.getConstructor().getModifiers()));
        var entity = new Disbursement();
        var amount = new BigDecimal("123.40");
        var date = LocalDateTime.of(2026, 11, 2, 12, 0);
        assertNull(entity.getStatus());
        assertNull(entity.getDisbursedAt());
        entity.setDisbursementId(1L);
        entity.setApplicationId(2L);
        entity.setApprovalId(3L);
        entity.setAmount(amount);
        entity.setStatus(DisbursementStatus.DISBURSED);
        entity.setTransactionReference("reference");
        entity.setProcessedBy("manager");
        entity.setDisbursedAt(date);
        assertEquals(1L, entity.getDisbursementId());
        assertEquals(2L, entity.getApplicationId());
        assertEquals(3L, entity.getApprovalId());
        assertSame(amount, entity.getAmount());
        assertEquals(DisbursementStatus.DISBURSED, entity.getStatus());
        assertEquals("reference", entity.getTransactionReference());
        assertEquals("manager", entity.getProcessedBy());
        assertSame(date, entity.getDisbursedAt());
        Set<String> methods = Arrays.stream(Disbursement.class.getDeclaredMethods())
                .map(method -> method.getName()).collect(Collectors.toSet());
        assertEquals(Set.of("getDisbursementId", "setDisbursementId", "getApplicationId", "setApplicationId",
                "getApprovalId", "setApprovalId", "getAmount", "setAmount", "getStatus", "setStatus",
                "getTransactionReference", "setTransactionReference", "getProcessedBy", "setProcessedBy",
                "getDisbursedAt", "setDisbursedAt"), methods);
    }
}

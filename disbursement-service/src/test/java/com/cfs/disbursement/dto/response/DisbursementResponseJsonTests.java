package com.cfs.disbursement.dto.response;

import com.cfs.disbursement.entity.Disbursement;
import com.cfs.disbursement.entity.DisbursementStatus;
import com.cfs.disbursement.mapper.DisbursementMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DisbursementResponseJsonTests {
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void mappedResponsePreservesExactJsonPropertiesAndValues() {
        var entity = new Disbursement();
        entity.setDisbursementId(1L);
        entity.setApplicationId(2L);
        entity.setApprovalId(3L);
        entity.setAmount(new BigDecimal("1234.50"));
        entity.setStatus(DisbursementStatus.DISBURSED);
        entity.setTransactionReference("CFSDIS-REFERENCE");
        entity.setProcessedBy("manager");
        entity.setDisbursedAt(LocalDateTime.of(2026, 11, 2, 12, 30, 45));
        var response = new DisbursementMapper().toResponse(entity);
        String encoded = mapper.writeValueAsString(response);
        var json = mapper.readTree(encoded);

        assertTrue(DisbursementResponse.class.isRecord());
        assertEquals(Set.of("disbursementId", "applicationId", "approvalId", "amount", "status",
                "transactionReference", "processedBy", "disbursedAt"), json.propertyNames());
        assertEquals(1L, json.get("disbursementId").longValue());
        assertEquals(2L, json.get("applicationId").longValue());
        assertEquals(3L, json.get("approvalId").longValue());
        assertTrue(json.get("amount").isNumber());
        assertEquals(0, new BigDecimal("1234.50").compareTo(json.get("amount").decimalValue()));
        assertTrue(encoded.contains("\"amount\":1234.50"));
        assertEquals("DISBURSED", json.get("status").stringValue());
        assertEquals("CFSDIS-REFERENCE", json.get("transactionReference").stringValue());
        assertEquals("manager", json.get("processedBy").stringValue());
        assertEquals("2026-11-02T12:30:45", json.get("disbursedAt").stringValue());
        assertEquals(response, mapper.readValue(encoded, DisbursementResponse.class));
    }

    @Test
    void nullableFieldsRemainNullAndPresent() {
        var response = new DisbursementResponse(null, null, null, null, null, null, null, null);
        var json = mapper.readTree(mapper.writeValueAsString(response));
        assertEquals(8, json.size());
        json.forEach(value -> assertTrue(value.isNull()));
    }
}

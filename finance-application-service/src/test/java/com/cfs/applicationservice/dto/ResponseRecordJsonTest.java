package com.cfs.applicationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.cfs.applicationservice.entity.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class ResponseRecordJsonTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void productPreservesExactFieldsDecimalNumbersAndActiveName() {
        LoanProductResponse response = new LoanProductResponse(1L, "PERSONAL", "Personal Loan",
                new BigDecimal("1000.25"), new BigDecimal("100000.50"), 6, 60,
                new BigDecimal("8.75"), new BigDecimal("15.25"), true);

        assertJson(response, """
                {"productId":1,"productCode":"PERSONAL","productName":"Personal Loan",
                 "minimumAmount":1000.25,"maximumAmount":100000.50,"minimumTenureMonths":6,
                 "maximumTenureMonths":60,"minimumInterestRate":8.75,"maximumInterestRate":15.25,"active":true}
                """);
        assertTrue(response.active());
        assertEquals(new BigDecimal("1000.25"), response.minimumAmount());
        assertEquals(new BigDecimal("100000.50"), response.maximumAmount());
        assertEquals(new BigDecimal("8.75"), response.minimumInterestRate());
        assertEquals(new BigDecimal("15.25"), response.maximumInterestRate());
    }

    @ParameterizedTest
    @EnumSource(ApplicationStatus.class)
    void applicationPreservesEveryEnumNameAsStringAndNullableSubmission(ApplicationStatus status) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 2, 12, 30, 45);
        FinanceApplicationResponse response = new FinanceApplicationResponse(2L, "APP-2", 3L, 1L,
                "Personal Loan", new BigDecimal("12345.67"), 24, "Education", status.name(), null, createdAt);

        assertJson(response, """
                {"applicationId":2,"applicationNumber":"APP-2","customerId":3,"productId":1,
                 "productName":"Personal Loan","requestedAmount":12345.67,"requestedTenureMonths":24,
                 "loanPurpose":"Education","status":"%s","submittedAt":null,"createdAt":"2026-11-02T12:30:45"}
                """.formatted(status.name()));
        assertEquals(new BigDecimal("12345.67"), response.requestedAmount());
        assertEquals(status.name(), response.status());
        assertNull(response.submittedAt());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void applicationPreservesNonNullSubmissionDate() {
        LocalDateTime submittedAt = LocalDateTime.of(2026, 11, 2, 13, 30, 45);
        FinanceApplicationResponse response = new FinanceApplicationResponse(
                null, null, null, null, null, null, null, null, "SUBMITTED", submittedAt, null);

        assertJson(response, """
                {"applicationId":null,"applicationNumber":null,"customerId":null,"productId":null,
                 "productName":null,"requestedAmount":null,"requestedTenureMonths":null,"loanPurpose":null,
                 "status":"SUBMITTED","submittedAt":"2026-11-02T13:30:45","createdAt":null}
                """);
        assertEquals(submittedAt, response.submittedAt());
    }

    @Test
    void historyPreservesNullableOldStatusEnumNameAndDate() {
        LocalDateTime changedAt = LocalDateTime.of(2026, 11, 2, 13, 30, 45);
        StatusHistoryResponse response = new StatusHistoryResponse(
                4L, 2L, null, ApplicationStatus.DRAFT.name(), "Created", changedAt);

        assertJson(response, """
                {"historyId":4,"applicationId":2,"oldStatus":null,"newStatus":"DRAFT",
                 "remarks":"Created","changedAt":"2026-11-02T13:30:45"}
                """);
        assertNull(response.oldStatus());
        assertEquals(ApplicationStatus.DRAFT.name(), response.newStatus());
        assertEquals(changedAt, response.changedAt());
    }

    @Test
    void noArgumentConstructorsRetainAllNullAndFalseDefaults() {
        assertJson(new LoanProductResponse(), """
                {"productId":null,"productCode":null,"productName":null,"minimumAmount":null,
                 "maximumAmount":null,"minimumTenureMonths":null,"maximumTenureMonths":null,
                 "minimumInterestRate":null,"maximumInterestRate":null,"active":false}
                """);
        assertJson(new FinanceApplicationResponse(), """
                {"applicationId":null,"applicationNumber":null,"customerId":null,"productId":null,
                 "productName":null,"requestedAmount":null,"requestedTenureMonths":null,"loanPurpose":null,
                 "status":null,"submittedAt":null,"createdAt":null}
                """);
        assertJson(new StatusHistoryResponse(), """
                {"historyId":null,"applicationId":null,"oldStatus":null,"newStatus":null,"remarks":null,"changedAt":null}
                """);
    }

    private void assertJson(Object response, String expected) {
        assertTrue(response.getClass().isRecord());
        var expectedTree = mapper.readTree(expected);
        assertEquals(expectedTree, mapper.readTree(mapper.writeValueAsString(response)));
        List<String> componentNames = Arrays.stream(response.getClass().getRecordComponents())
                .map(component -> component.getName()).toList();
        assertEquals(List.copyOf(expectedTree.propertyNames()), componentNames);
    }
}

package com.cfs.emi.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.cfs.emi.entity.EmiStatus;
import com.cfs.emi.entity.LoanAccountStatus;
import com.cfs.emi.entity.PaymentMethod;
import com.cfs.emi.entity.RepaymentStatus;

import tools.jackson.databind.json.JsonMapper;

class ResponseRecordSerializationTest {

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    void emiInstallmentResponsePreservesPropertiesValuesAndNulls() throws Exception {
        EmiInstallmentResponse response = new EmiInstallmentResponse(
                1L, 2L, 3, LocalDate.of(2026, 1, 2),
                new BigDecimal("100.00"), new BigDecimal("90.00"), new BigDecimal("10.00"),
                new BigDecimal("100.00"), new BigDecimal("0.00"), EmiStatus.PENDING, null);

        assertEquals(
                "{\"emiId\":1,\"loanAccountId\":2,\"installmentNumber\":3,\"dueDate\":\"2026-01-02\","
                        + "\"openingPrincipal\":100.00,\"principalComponent\":90.00,\"interestComponent\":10.00,"
                        + "\"emiAmount\":100.00,\"amountPaid\":0.00,\"status\":\"PENDING\",\"paidAt\":null}",
                jsonMapper.writeValueAsString(response));
    }

    @Test
    void loanAccountResponsePreservesPropertiesValuesAndNulls() throws Exception {
        LoanAccountResponse response = new LoanAccountResponse(
                1L, 2L, 3L, null, new BigDecimal("100.00"), new BigDecimal("5.2500"), 12,
                new BigDecimal("8.75"), new BigDecimal("105.00"), new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 2), LocalDate.of(2027, 1, 2), LoanAccountStatus.ACTIVE);

        assertEquals(
                "{\"loanAccountId\":1,\"applicationId\":2,\"customerId\":3,\"disbursementId\":null,"
                        + "\"principalAmount\":100.00,\"annualInterestRate\":5.2500,\"tenureMonths\":12,"
                        + "\"monthlyEmiAmount\":8.75,\"totalPayableAmount\":105.00,\"outstandingAmount\":100.00,"
                        + "\"startDate\":\"2026-01-02\",\"maturityDate\":\"2027-01-02\",\"status\":\"ACTIVE\"}",
                jsonMapper.writeValueAsString(response));
    }

    @Test
    void repaymentResponsePreservesPropertiesValuesAndNulls() throws Exception {
        RepaymentResponse response = new RepaymentResponse(
                1L, 2L, 3L, null, "PAY-1", new BigDecimal("10.00"),
                LocalDateTime.of(2026, 1, 2, 3, 4, 5), PaymentMethod.SIMULATED,
                RepaymentStatus.SUCCESS, null, null);

        assertEquals(
                "{\"repaymentId\":1,\"loanAccountId\":2,\"emiId\":3,\"installmentNumber\":null,"
                        + "\"paymentReference\":\"PAY-1\",\"amount\":10.00,"
                        + "\"paymentDate\":\"2026-01-02T03:04:05\",\"paymentMethod\":\"SIMULATED\","
                        + "\"status\":\"SUCCESS\",\"recordedBy\":null,\"remarks\":null}",
                jsonMapper.writeValueAsString(response));
    }
}

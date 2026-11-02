package com.cfs.customerservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class ResponseRecordJsonTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void addressPreservesExactFieldsAndNullableAddressLine() {
        AddressResponse response = new AddressResponse(1L, 2L, "HOME", "Main Street", null,
                "Pune", "Maharashtra", "411001", "India");

        assertJson(response, """
                {"addressId":1,"customerId":2,"addressType":"HOME","addressLine1":"Main Street",
                 "addressLine2":null,"city":"Pune","state":"Maharashtra","pincode":"411001","country":"India"}
                """);
        assertEquals(1L, response.addressId());
        assertNull(response.addressLine2());
    }

    @Test
    void documentPreservesDateAndStatusString() {
        LocalDateTime uploadedAt = LocalDateTime.of(2026, 11, 2, 12, 30, 45);
        DocumentResponse response = new DocumentResponse(3L, 2L, "PAN", "pan.pdf",
                "documents/pan.pdf", "VERIFIED", uploadedAt);

        assertJson(response, """
                {"documentId":3,"customerId":2,"documentType":"PAN","fileName":"pan.pdf",
                 "filePath":"documents/pan.pdf","verificationStatus":"VERIFIED",
                 "uploadedAt":"2026-11-02T12:30:45"}
                """);
        assertEquals(uploadedAt, response.uploadedAt());
        assertEquals("VERIFIED", response.verificationStatus());
    }

    @Test
    void employmentPreservesDecimalNumbersAndNullableExperience() {
        EmploymentResponse response = new EmploymentResponse(4L, 2L, "SALARIED", "Employer",
                "Engineer", new BigDecimal("123456.78"), new BigDecimal("4321.09"), null);

        assertJson(response, """
                {"employmentId":4,"customerId":2,"employmentType":"SALARIED","employerName":"Employer",
                 "designation":"Engineer","monthlyIncome":123456.78,"existingMonthlyEmi":4321.09,
                 "workExperienceMonths":null}
                """);
        assertEquals(new BigDecimal("123456.78"), response.monthlyIncome());
        assertEquals(new BigDecimal("4321.09"), response.existingMonthlyEmi());
        assertNull(response.workExperienceMonths());
    }

    @Test
    void onboardingPreservesBooleanNamesAndBothBooleanValues() {
        OnboardingStatusResponse response = new OnboardingStatusResponse(
                2L, true, false, true, false, true, "VERIFIED", "IN_PROGRESS");

        assertJson(response, """
                {"customerId":2,"profileCompleted":true,"addressCompleted":false,"employmentCompleted":true,
                 "bankCompleted":false,"documentsCompleted":true,"kycStatus":"VERIFIED","onboardingStatus":"IN_PROGRESS"}
                """);
        assertTrue(response.profileCompleted());
        assertFalse(response.addressCompleted());
        assertTrue(response.employmentCompleted());
        assertFalse(response.bankCompleted());
        assertTrue(response.documentsCompleted());
    }

    @Test
    void noArgumentConstructorsRetainAllNullAndFalseDefaults() {
        assertJson(new AddressResponse(), """
                {"addressId":null,"customerId":null,"addressType":null,"addressLine1":null,
                 "addressLine2":null,"city":null,"state":null,"pincode":null,"country":null}
                """);
        assertJson(new DocumentResponse(), """
                {"documentId":null,"customerId":null,"documentType":null,"fileName":null,
                 "filePath":null,"verificationStatus":null,"uploadedAt":null}
                """);
        assertJson(new EmploymentResponse(), """
                {"employmentId":null,"customerId":null,"employmentType":null,"employerName":null,
                 "designation":null,"monthlyIncome":null,"existingMonthlyEmi":null,"workExperienceMonths":null}
                """);
        assertJson(new OnboardingStatusResponse(), """
                {"customerId":null,"profileCompleted":false,"addressCompleted":false,"employmentCompleted":false,
                 "bankCompleted":false,"documentsCompleted":false,"kycStatus":null,"onboardingStatus":null}
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

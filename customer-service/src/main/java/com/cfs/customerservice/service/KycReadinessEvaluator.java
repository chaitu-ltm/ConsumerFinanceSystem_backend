package com.cfs.customerservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cfs.customerservice.entity.Customer;
import com.cfs.customerservice.exceptions.KycPrerequisitesMissingException;
import com.cfs.customerservice.repository.BankAccountRepository;
import com.cfs.customerservice.repository.CustomerAddressRepository;
import com.cfs.customerservice.repository.CustomerDocumentRepository;
import com.cfs.customerservice.repository.EmploymentDetailRepository;
import lombok.RequiredArgsConstructor;

/**
 * Single source of truth for KYC submission readiness and the
 * onboarding status derived from KYC status plus readiness.
 */
@Component
@RequiredArgsConstructor
public class KycReadinessEvaluator {

    public static final List<String> REQUIRED_DOCUMENT_TYPES = List.of(
            "PAN",
            "IDENTITY_PROOF",
            "ADDRESS_PROOF",
            "SALARY_SLIP",
            "BANK_STATEMENT");

    private final CustomerAddressRepository addressRepository;

    private final EmploymentDetailRepository employmentRepository;

    private final BankAccountRepository bankAccountRepository;

    private final CustomerDocumentRepository documentRepository;

    public Readiness evaluate(Customer customer) {

        if (customer == null || customer.getId() == null) {
            return new Readiness(false, false, false, false,
                    List.of("PROFILE", "ADDRESS", "EMPLOYMENT", "BANK_ACCOUNT"),
                    REQUIRED_DOCUMENT_TYPES);
        }

        Long customerId = customer.getId();

        boolean addressCompleted =
                !addressRepository.findByCustomerId(customerId).isEmpty();

        boolean employmentCompleted =
                employmentRepository.existsByCustomerId(customerId);

        boolean bankCompleted =
                bankAccountRepository.existsByCustomerId(customerId);

        Set<String> uploadedTypes = documentRepository
                .findByCustomerId(customerId)
                .stream()
                .map(document -> document.getDocumentType())
                .filter(type -> type != null)
                .map(type -> type.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<String> missingDocumentTypes = REQUIRED_DOCUMENT_TYPES
                .stream()
                .filter(type -> !uploadedTypes.contains(type))
                .toList();

        List<String> missingSections = new ArrayList<>();

        if (!addressCompleted) {
            missingSections.add("ADDRESS");
        }
        if (!employmentCompleted) {
            missingSections.add("EMPLOYMENT");
        }
        if (!bankCompleted) {
            missingSections.add("BANK_ACCOUNT");
        }
        if (!missingDocumentTypes.isEmpty()) {
            missingSections.add("DOCUMENTS");
        }

        return new Readiness(true, addressCompleted, employmentCompleted,
                bankCompleted, missingSections, missingDocumentTypes);
    }

    public void assertReady(Readiness readiness) {

        if (!readiness.isReady()) {
            throw new KycPrerequisitesMissingException(
                    readiness.getMissingSections(),
                    readiness.getMissingDocumentTypes());
        }
    }

    public String resolveOnboardingStatus(
            String kycStatus,
            Readiness readiness) {

        String normalized = kycStatus == null
                ? ""
                : kycStatus.trim().toUpperCase(Locale.ROOT);

        if ("REJECTED".equals(normalized)) {
            return "ACTION_REQUIRED";
        }

        if ("VERIFIED".equals(normalized) && readiness.isReady()) {
            return "COMPLETED";
        }

        return "IN_PROGRESS";
    }

    public static final class Readiness {

        private final boolean profileCompleted;
        private final boolean addressCompleted;
        private final boolean employmentCompleted;
        private final boolean bankCompleted;
        private final List<String> missingSections;
        private final List<String> missingDocumentTypes;

        public Readiness(
                boolean profileCompleted,
                boolean addressCompleted,
                boolean employmentCompleted,
                boolean bankCompleted,
                List<String> missingSections,
                List<String> missingDocumentTypes) {

            this.profileCompleted = profileCompleted;
            this.addressCompleted = addressCompleted;
            this.employmentCompleted = employmentCompleted;
            this.bankCompleted = bankCompleted;
            this.missingSections = Collections.unmodifiableList(new ArrayList<>(missingSections));
            this.missingDocumentTypes = Collections.unmodifiableList(new ArrayList<>(missingDocumentTypes));
        }

        public boolean isProfileCompleted() {
            return profileCompleted;
        }

        public boolean isAddressCompleted() {
            return addressCompleted;
        }

        public boolean isEmploymentCompleted() {
            return employmentCompleted;
        }

        public boolean isBankCompleted() {
            return bankCompleted;
        }

        public boolean isDocumentsCompleted() {
            return missingDocumentTypes.isEmpty();
        }

        public boolean isReady() {
            return profileCompleted
                    && addressCompleted
                    && employmentCompleted
                    && bankCompleted
                    && isDocumentsCompleted();
        }

        public List<String> getMissingSections() {
            return missingSections;
        }

        public List<String> getMissingDocumentTypes() {
            return missingDocumentTypes;
        }
    }
}

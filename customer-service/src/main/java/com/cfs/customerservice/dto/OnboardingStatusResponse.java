package com.cfs.customerservice.dto;

public record OnboardingStatusResponse(
        Long customerId,
        boolean profileCompleted,
        boolean addressCompleted,
        boolean employmentCompleted,
        boolean bankCompleted,
        boolean documentsCompleted,
        String kycStatus,
        String onboardingStatus) {

    public OnboardingStatusResponse() {
        this(null, false, false, false, false, false, null, null);
    }
}

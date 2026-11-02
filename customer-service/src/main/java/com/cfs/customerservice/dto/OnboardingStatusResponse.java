package com.cfs.customerservice.dto;

public class OnboardingStatusResponse {

    private Long customerId;

    private boolean profileCompleted;

    private boolean addressCompleted;

    private boolean employmentCompleted;

    private boolean bankCompleted;

    private boolean documentsCompleted;

    private String kycStatus;

    private String onboardingStatus;

    public OnboardingStatusResponse() {
    }

    public OnboardingStatusResponse(
            Long customerId,
            boolean profileCompleted,
            boolean addressCompleted,
            boolean employmentCompleted,
            boolean bankCompleted,
            boolean documentsCompleted,
            String kycStatus,
            String onboardingStatus) {

        this.customerId = customerId;
        this.profileCompleted = profileCompleted;
        this.addressCompleted = addressCompleted;
        this.employmentCompleted = employmentCompleted;
        this.bankCompleted = bankCompleted;
        this.documentsCompleted = documentsCompleted;
        this.kycStatus = kycStatus;
        this.onboardingStatus = onboardingStatus;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(
            boolean profileCompleted) {

        this.profileCompleted = profileCompleted;
    }

    public boolean isAddressCompleted() {
        return addressCompleted;
    }

    public void setAddressCompleted(
            boolean addressCompleted) {

        this.addressCompleted = addressCompleted;
    }

    public boolean isEmploymentCompleted() {
        return employmentCompleted;
    }

    public void setEmploymentCompleted(
            boolean employmentCompleted) {

        this.employmentCompleted = employmentCompleted;
    }

    public boolean isBankCompleted() {
        return bankCompleted;
    }

    public void setBankCompleted(
            boolean bankCompleted) {

        this.bankCompleted = bankCompleted;
    }

    public boolean isDocumentsCompleted() {
        return documentsCompleted;
    }

    public void setDocumentsCompleted(
            boolean documentsCompleted) {

        this.documentsCompleted = documentsCompleted;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public String getOnboardingStatus() {
        return onboardingStatus;
    }

    public void setOnboardingStatus(
            String onboardingStatus) {

        this.onboardingStatus = onboardingStatus;
    }
}
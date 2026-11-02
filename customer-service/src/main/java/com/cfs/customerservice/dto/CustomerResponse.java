package com.cfs.customerservice.dto;

import java.time.LocalDate;

public class CustomerResponse {

    private Long customerId;

    private Long userId;

    private String customerNumber;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String mobileNumber;

    private String panNumber;

    private String kycStatus;

    private String onboardingStatus;


    public CustomerResponse() {
    }


    public CustomerResponse(
            Long customerId,
            Long userId,
            String customerNumber,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String mobileNumber,
            String panNumber,
            String kycStatus,
            String onboardingStatus) {

        this.customerId = customerId;
        this.userId = userId;
        this.customerNumber = customerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.mobileNumber = mobileNumber;
        this.panNumber = panNumber;
        this.kycStatus = kycStatus;
        this.onboardingStatus = onboardingStatus;
    }


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }


    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
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

    public void setOnboardingStatus(String onboardingStatus) {
        this.onboardingStatus = onboardingStatus;
    }
}
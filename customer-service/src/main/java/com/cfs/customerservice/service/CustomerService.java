package com.cfs.customerservice.service;

import java.util.List;

import com.cfs.customerservice.dto.AddressRequest;
import com.cfs.customerservice.dto.AddressResponse;
import com.cfs.customerservice.dto.CustomerRequest;
import com.cfs.customerservice.dto.CustomerResponse;
import com.cfs.customerservice.dto.DocumentRequest;
import com.cfs.customerservice.dto.DocumentResponse;
import com.cfs.customerservice.dto.EmploymentRequest;
import com.cfs.customerservice.dto.EmploymentResponse;
import com.cfs.customerservice.dto.KycStatusRequest;
import com.cfs.customerservice.dto.OnboardingStatusResponse;
import com.cfs.customerservice.dto.BankAccountRequest;
import com.cfs.customerservice.dto.BankAccountResponse;

public interface CustomerService {

	CustomerResponse createCustomer(CustomerRequest request);

	CustomerResponse getCustomer(Long customerId);

	CustomerResponse getCustomerByUserId(Long userId);

	AddressResponse addAddress(Long customerId, AddressRequest request);

	List<AddressResponse> getAddresses(Long customerId);

	EmploymentResponse addEmployment(Long customerId, EmploymentRequest request);

	EmploymentResponse getEmployment(Long customerId);

	BankAccountResponse addBankAccount(Long customerId, BankAccountRequest request);

	List<BankAccountResponse> getBankAccounts(Long customerId);

	DocumentResponse addDocument(Long customerId, DocumentRequest request);

	List<DocumentResponse> getDocuments(Long customerId);

	CustomerResponse updateKycStatus(Long customerId, KycStatusRequest request);

	CustomerResponse requestKycReview(Long customerId);

	OnboardingStatusResponse getOnboardingStatus(Long customerId);
}
package com.cfs.customerservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.customerservice.dto.AdminKycResponse;
import com.cfs.customerservice.dto.KycActionResponse;
import com.cfs.customerservice.dto.KycDocumentResponse;
import com.cfs.customerservice.dto.KycRejectRequest;
import com.cfs.customerservice.entity.Customer;
import com.cfs.customerservice.entity.CustomerDocument;
import com.cfs.customerservice.exceptions.ResourceNotFoundException;
import com.cfs.customerservice.exceptions.WorkflowConflictException;
import com.cfs.customerservice.repository.CustomerDocumentRepository;
import com.cfs.customerservice.repository.CustomerRepository;

@Service
public class AdminKycServiceImpl implements AdminKycService {

    private final CustomerRepository customerRepository;
    private final CustomerDocumentRepository customerDocumentRepository;
    private final KycReadinessEvaluator kycReadinessEvaluator;

    public AdminKycServiceImpl(
            CustomerRepository customerRepository,
            CustomerDocumentRepository customerDocumentRepository,
            KycReadinessEvaluator kycReadinessEvaluator) {

        this.customerRepository = customerRepository;
        this.customerDocumentRepository =
                customerDocumentRepository;
        this.kycReadinessEvaluator = kycReadinessEvaluator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminKycResponse> getAllKyc() {

        List<AdminKycResponse> responses = new ArrayList<>();

        for (Customer customer : customerRepository.findAll()) {
            responses.add(buildResponse(customer));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminKycResponse> getPendingKyc() {

        // Admin queue = customers who submitted KYC and await a decision.
        List<Customer> customers =
                customerRepository.findByKycStatus("UNDER_REVIEW");

        List<AdminKycResponse> responses =
                new ArrayList<>();

        for (Customer customer : customers) {
            responses.add(
                    buildResponse(customer)
            );
        }

        return responses;
    }

    @Override
    public AdminKycResponse getKycDetails(
            Long customerId) {

        Customer customer = getCustomer(customerId);

        return buildResponse(customer);
    }

    @Override
    @Transactional
    public KycActionResponse verifyKyc(
            Long customerId) {

        Customer customer = getCustomerForUpdate(customerId);

        requireUnderReview(customer, "verified");

        KycReadinessEvaluator.Readiness readiness =
                kycReadinessEvaluator.evaluate(customer);

        kycReadinessEvaluator.assertReady(readiness);

        List<CustomerDocument> documents =
                customerDocumentRepository
                        .findByCustomerId(customerId);

        for (CustomerDocument document : documents) {
            document.setVerificationStatus("VERIFIED");
        }

        customerDocumentRepository.saveAll(documents);

        customer.setKycStatus("VERIFIED");
        customer.setOnboardingStatus(
                kycReadinessEvaluator.resolveOnboardingStatus(
                        customer.getKycStatus(),
                        readiness
                )
        );

        customerRepository.save(customer);

        KycActionResponse response =
                new KycActionResponse();

        response.setCustomerId(customer.getId());
        response.setKycStatus(customer.getKycStatus());
        response.setOnboardingStatus(
                customer.getOnboardingStatus()
        );
        response.setMessage(
                "KYC verified successfully"
        );

        return response;
    }

    @Override
    @Transactional
    public KycActionResponse rejectKyc(
            Long customerId,
            KycRejectRequest request) {

        Customer customer = getCustomerForUpdate(customerId);

        requireUnderReview(customer, "rejected");

        if (request == null
                || request.getReason() == null
                || request.getReason().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Rejection reason is required"
            );
        }

        customer.setKycStatus("REJECTED");
        customer.setOnboardingStatus(
                kycReadinessEvaluator.resolveOnboardingStatus(
                        customer.getKycStatus(),
                        kycReadinessEvaluator.evaluate(customer)
                )
        );

        customerRepository.save(customer);

        KycActionResponse response =
                new KycActionResponse();

        response.setCustomerId(customer.getId());
        response.setKycStatus(customer.getKycStatus());
        response.setOnboardingStatus(
                customer.getOnboardingStatus()
        );
        response.setReason(request.getReason());
        response.setMessage("KYC rejected");

        return response;
    }

    private Customer getCustomer(Long customerId) {

        return customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found with id: "
                                        + customerId
                        )
                );
    }

    private Customer getCustomerForUpdate(Long customerId) {

        return customerRepository
                .findByIdForUpdate(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: "
                                        + customerId
                        )
                );
    }

    private void requireUnderReview(
            Customer customer,
            String action) {

        if (!"UNDER_REVIEW".equalsIgnoreCase(
                customer.getKycStatus())) {

            throw new WorkflowConflictException(
                    "KYC can only be " + action
                            + " when status is UNDER_REVIEW; current status is "
                            + customer.getKycStatus()
            );
        }
    }

    private AdminKycResponse buildResponse(
            Customer customer) {

        AdminKycResponse response =
                new AdminKycResponse();

        response.setCustomerId(customer.getId());
        response.setUserId(customer.getUserId());
        response.setCustomerNumber(
                customer.getCustomerNumber()
        );
        response.setFirstName(
                customer.getFirstName()
        );
        response.setLastName(
                customer.getLastName()
        );
        response.setDateOfBirth(
                customer.getDateOfBirth()
        );
        response.setMobileNumber(
                customer.getMobileNumber()
        );
        response.setPanNumber(
                customer.getPanNumber()
        );
        response.setKycStatus(
                customer.getKycStatus()
        );
        response.setOnboardingStatus(
                customer.getOnboardingStatus()
        );

        List<CustomerDocument> documents =
                customerDocumentRepository
                        .findByCustomerId(
                                customer.getId()
                        );

        List<KycDocumentResponse> documentResponses =
                new ArrayList<>();

        for (CustomerDocument document : documents) {

            KycDocumentResponse documentResponse =
                    new KycDocumentResponse();

            documentResponse.setId(
                    document.getId()
            );
            documentResponse.setDocumentType(
                    document.getDocumentType()
            );
            documentResponse.setFileName(
                    document.getFileName()
            );
            documentResponse.setFilePath(
                    document.getFilePath()
            );
            documentResponse.setVerificationStatus(
                    document.getVerificationStatus()
            );
            documentResponse.setUploadedAt(
                    document.getUploadedAt()
            );

            documentResponses.add(documentResponse);
        }

        response.setDocuments(documentResponses);

        return response;
    }
    
}
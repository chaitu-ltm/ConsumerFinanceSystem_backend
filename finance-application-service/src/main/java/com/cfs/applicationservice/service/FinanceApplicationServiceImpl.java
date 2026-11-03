package com.cfs.applicationservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.applicationservice.client.CustomerServiceClient;

import com.cfs.applicationservice.dto.ApplicationAssessmentContextResponse;
import com.cfs.applicationservice.dto.AssessmentQueueItemResponse;
import com.cfs.applicationservice.dto.CustomerOnboardingResponse;
import com.cfs.applicationservice.dto.FinanceApplicationRequest;
import com.cfs.applicationservice.dto.FinanceApplicationResponse;
import com.cfs.applicationservice.dto.StatusHistoryResponse;

import com.cfs.applicationservice.entity.ApplicationStatus;
import com.cfs.applicationservice.entity.ApplicationStatusHistory;
import com.cfs.applicationservice.entity.FinanceApplication;
import com.cfs.applicationservice.entity.LoanProduct;

import com.cfs.applicationservice.exception.CustomerNotEligibleException;
import com.cfs.applicationservice.exception.CustomerServiceUnavailableException;
import com.cfs.applicationservice.exception.InvalidApplicationStateException;
import com.cfs.applicationservice.exception.ResourceNotFoundException;

import com.cfs.applicationservice.repository.ApplicationStatusHistoryRepository;
import com.cfs.applicationservice.repository.FinanceApplicationRepository;
import com.cfs.applicationservice.repository.LoanProductRepository;

import feign.FeignException;

@Service
@Transactional
public class FinanceApplicationServiceImpl
        implements FinanceApplicationService {

    private final FinanceApplicationRepository
            applicationRepository;

    private final LoanProductRepository
            loanProductRepository;

    private final ApplicationStatusHistoryRepository
            historyRepository;

    private final CustomerServiceClient
            customerServiceClient;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FinanceApplicationServiceImpl(
            FinanceApplicationRepository applicationRepository,
            LoanProductRepository loanProductRepository,
            ApplicationStatusHistoryRepository historyRepository,
            CustomerServiceClient customerServiceClient) {

        this.applicationRepository =
                applicationRepository;

        this.loanProductRepository =
                loanProductRepository;

        this.historyRepository =
                historyRepository;

        this.customerServiceClient =
                customerServiceClient;
    }


    // =====================================================
    // CREATE DRAFT APPLICATION
    // =====================================================

    @Override
    public FinanceApplicationResponse createApplication(
            FinanceApplicationRequest request) {

        LoanProduct product =
                findProduct(
                        request.getProductId()
                );


        if (!product.isActive()) {

            throw new IllegalArgumentException(
                    "Selected loan product is inactive"
            );
        }


        validateAmount(
                request,
                product
        );


        validateTenure(
                request,
                product
        );


        FinanceApplication application =
                new FinanceApplication();


        application.setApplicationNumber(
                generateApplicationNumber()
        );


        application.setCustomerId(
                request.getCustomerId()
        );


        application.setProductId(
                request.getProductId()
        );


        application.setRequestedAmount(
                request.getRequestedAmount()
        );


        application.setRequestedTenureMonths(
                request.getRequestedTenureMonths()
        );


        application.setLoanPurpose(
                request.getLoanPurpose()
                        .trim()
        );


        application.setStatus(
                ApplicationStatus.DRAFT
        );


        FinanceApplication savedApplication =
                applicationRepository.save(
                        application
                );


        saveHistory(
                savedApplication.getId(),
                null,
                ApplicationStatus.DRAFT,
                "Loan application created"
        );


        return mapApplication(
                savedApplication,
                product
        );
    }


    // =====================================================
    // GET APPLICATION BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentQueueItemResponse> getAssessmentQueue(String status) {
        if (status != null && !ApplicationStatus.SUBMITTED.name().equals(status)) {
            throw new IllegalArgumentException("Only SUBMITTED status is supported for the assessment queue");
        }
        List<FinanceApplication> applications =
                applicationRepository.findByStatus(ApplicationStatus.SUBMITTED);
        if (applications.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = applications.stream().map(FinanceApplication::getProductId).distinct().toList();
        Map<Long, LoanProduct> products = loanProductRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(LoanProduct::getId, Function.identity()));
        return applications.stream().map(application -> {
            LoanProduct product = products.get(application.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException(
                        "Loan product not found with ID: " + application.getProductId());
            }
            return new AssessmentQueueItemResponse(
                    application.getId(), application.getApplicationNumber(), application.getCustomerId(),
                    application.getProductId(), product.getProductName(), application.getRequestedAmount(),
                    application.getRequestedTenureMonths(), application.getLoanPurpose(),
                    application.getSubmittedAt(), application.getStatus().name());
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationAssessmentContextResponse getAssessmentContext(Long applicationId) {
        FinanceApplication application = findApplication(applicationId);
        return new ApplicationAssessmentContextResponse(
                application.getId(), application.getStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceApplicationResponse getApplication(
            Long applicationId) {

        FinanceApplication application =
                findApplication(
                        applicationId
                );


        LoanProduct product =
                findProduct(
                        application.getProductId()
                );


        return mapApplication(
                application,
                product
        );
    }


    // =====================================================
    // GET APPLICATIONS BY CUSTOMER
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<FinanceApplicationResponse>
            getApplicationsByCustomer(
            Long customerId) {

        List<FinanceApplication> applications =
                applicationRepository
                        .findByCustomerId(
                                customerId
                        );


        return applications
                .stream()
                .map(application -> {

                    LoanProduct product =
                            findProduct(
                                    application
                                            .getProductId()
                            );


                    return mapApplication(
                            application,
                            product
                    );
                })
                .toList();
    }


    // =====================================================
    // SUBMIT APPLICATION
    // =====================================================

    @Override
    public FinanceApplicationResponse submitApplication(
            Long applicationId) {

        // -----------------------------------------
        // Find application
        // -----------------------------------------

        FinanceApplication application =
                findApplication(
                        applicationId
                );


        // -----------------------------------------
        // Application must be DRAFT
        // -----------------------------------------

        if (application.getStatus()
                != ApplicationStatus.DRAFT) {

            throw new InvalidApplicationStateException(
                    "Only a DRAFT application can be submitted"
            );
        }


        // -----------------------------------------
        // Find Loan Product
        // -----------------------------------------

        LoanProduct product =
                findProduct(
                        application.getProductId()
                );


        // -----------------------------------------
        // Product must still be ACTIVE
        // -----------------------------------------

        if (!product.isActive()) {

            throw new InvalidApplicationStateException(
                    "Loan product is no longer active"
            );
        }


        // -----------------------------------------
        // Call Customer Service through Feign
        // -----------------------------------------

        CustomerOnboardingResponse onboarding;


        try {

            onboarding =
                    customerServiceClient
                            .getOnboardingStatus(
                                    application
                                            .getCustomerId()
                            );

        } catch (FeignException.NotFound exception) {

            throw new CustomerNotEligibleException(
                    "Customer profile was not found"
            );

        } catch (FeignException exception) {

            throw new CustomerServiceUnavailableException(
                    "Unable to verify customer onboarding at this time"
            );
        }


        // -----------------------------------------
        // Null response protection
        // -----------------------------------------

        if (onboarding == null) {

            throw new CustomerServiceUnavailableException(
                    "Customer onboarding response is unavailable"
            );
        }


        // -----------------------------------------
        // Customer onboarding validation
        // -----------------------------------------

        if (!"COMPLETED".equalsIgnoreCase(
                onboarding.getOnboardingStatus())) {

            throw new CustomerNotEligibleException(
                    "Customer onboarding is not completed"
            );
        }


        // -----------------------------------------
        // Customer KYC validation
        // -----------------------------------------

        if (!"VERIFIED".equalsIgnoreCase(
                onboarding.getKycStatus())) {

            throw new CustomerNotEligibleException(
                    "Customer KYC is not verified"
            );
        }


        // -----------------------------------------
        // Change DRAFT → SUBMITTED
        // -----------------------------------------

        ApplicationStatus oldStatus =
                application.getStatus();


        application.setStatus(
                ApplicationStatus.SUBMITTED
        );


        application.setSubmittedAt(
                LocalDateTime.now()
        );


        FinanceApplication savedApplication =
                applicationRepository.save(
                        application
                );


        // -----------------------------------------
        // Save status history
        // -----------------------------------------

        saveHistory(
                savedApplication.getId(),
                oldStatus,
                ApplicationStatus.SUBMITTED,
                "Application submitted after customer onboarding verification"
        );


        return mapApplication(
                savedApplication,
                product
        );
    }


    // =====================================================
    // GET APPLICATION STATUS HISTORY
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse>
            getStatusHistory(
            Long applicationId) {

        // Check application exists
        findApplication(
                applicationId
        );


        return historyRepository
                .findByApplicationIdOrderByChangedAtAsc(
                        applicationId
                )
                .stream()
                .map(this::mapHistory)
                .toList();
    }


    // =====================================================
    // VALIDATE REQUESTED AMOUNT
    // =====================================================

    private void validateAmount(
            FinanceApplicationRequest request,
            LoanProduct product) {


        // Amount below minimum
        if (request.getRequestedAmount()
                .compareTo(
                        product.getMinimumAmount()
                ) < 0) {

            throw new IllegalArgumentException(
                    "Requested amount is below product minimum amount"
            );
        }


        // Amount greater than maximum
        if (request.getRequestedAmount()
                .compareTo(
                        product.getMaximumAmount()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Requested amount exceeds product maximum amount"
            );
        }
    }


    // =====================================================
    // VALIDATE REQUESTED TENURE
    // =====================================================

    private void validateTenure(
            FinanceApplicationRequest request,
            LoanProduct product) {


        // Tenure below minimum
        if (request.getRequestedTenureMonths()
                < product.getMinimumTenureMonths()) {

            throw new IllegalArgumentException(
                    "Requested tenure is below product minimum tenure"
            );
        }


        // Tenure above maximum
        if (request.getRequestedTenureMonths()
                > product.getMaximumTenureMonths()) {

            throw new IllegalArgumentException(
                    "Requested tenure exceeds product maximum tenure"
            );
        }
    }


    // =====================================================
    // SAVE STATUS HISTORY
    // =====================================================

    private void saveHistory(
            Long applicationId,
            ApplicationStatus oldStatus,
            ApplicationStatus newStatus,
            String remarks) {


        ApplicationStatusHistory history =
                new ApplicationStatusHistory();


        history.setApplicationId(
                applicationId
        );


        history.setOldStatus(
                oldStatus
        );


        history.setNewStatus(
                newStatus
        );


        history.setRemarks(
                remarks
        );


        historyRepository.save(
                history
        );
    }


    // =====================================================
    // FIND APPLICATION
    // =====================================================

    private FinanceApplication findApplication(
            Long applicationId) {


        return applicationRepository
                .findById(
                        applicationId
                )
                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Loan application not found with ID: "
                                        + applicationId
                        )
                );
    }


    // =====================================================
    // FIND LOAN PRODUCT
    // =====================================================

    private LoanProduct findProduct(
            Long productId) {


        return loanProductRepository
                .findById(
                        productId
                )
                .orElseThrow(() ->

                        new ResourceNotFoundException(
                                "Loan product not found with ID: "
                                        + productId
                        )
                );
    }


    // =====================================================
    // GENERATE APPLICATION NUMBER
    // =====================================================

    private String generateApplicationNumber() {


        return "CFSAPP"
                + System.currentTimeMillis();
    }


    // =====================================================
    // MAP APPLICATION → RESPONSE
    // =====================================================

    private FinanceApplicationResponse
            mapApplication(
            FinanceApplication application,
            LoanProduct product) {


        return new FinanceApplicationResponse(

                application.getId(),

                application.getApplicationNumber(),

                application.getCustomerId(),

                application.getProductId(),

                product.getProductName(),

                application.getRequestedAmount(),

                application.getRequestedTenureMonths(),

                application.getLoanPurpose(),

                application.getStatus().name(),

                application.getSubmittedAt(),

                application.getCreatedAt()
        );
    }


    // =====================================================
    // MAP HISTORY → RESPONSE
    // =====================================================

    private StatusHistoryResponse mapHistory(
            ApplicationStatusHistory history) {


        String oldStatus = null;


        if (history.getOldStatus() != null) {

            oldStatus =
                    history.getOldStatus()
                            .name();
        }


        return new StatusHistoryResponse(

                history.getId(),

                history.getApplicationId(),

                oldStatus,

                history.getNewStatus()
                        .name(),

                history.getRemarks(),

                history.getChangedAt()
        );
    }
}
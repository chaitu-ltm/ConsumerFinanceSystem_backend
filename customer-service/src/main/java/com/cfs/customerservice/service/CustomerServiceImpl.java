package com.cfs.customerservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.customerservice.dto.AddressRequest;
import com.cfs.customerservice.dto.AddressResponse;
import com.cfs.customerservice.dto.BankAccountRequest;
import com.cfs.customerservice.dto.BankAccountResponse;
import com.cfs.customerservice.dto.CustomerRequest;
import com.cfs.customerservice.dto.CustomerResponse;
import com.cfs.customerservice.dto.DocumentRequest;
import com.cfs.customerservice.dto.DocumentResponse;
import com.cfs.customerservice.dto.EmploymentRequest;
import com.cfs.customerservice.dto.EmploymentResponse;
import com.cfs.customerservice.dto.KycRejectRequest;
import com.cfs.customerservice.dto.KycStatusRequest;
import com.cfs.customerservice.dto.OnboardingStatusResponse;

import com.cfs.customerservice.entity.BankAccount;
import com.cfs.customerservice.entity.Customer;
import com.cfs.customerservice.entity.CustomerAddress;
import com.cfs.customerservice.entity.CustomerDocument;
import com.cfs.customerservice.entity.EmploymentDetail;

import com.cfs.customerservice.exceptions.DuplicateResourceException;
import com.cfs.customerservice.exceptions.ResourceNotFoundException;
import com.cfs.customerservice.exceptions.WorkflowConflictException;

import com.cfs.customerservice.repository.BankAccountRepository;
import com.cfs.customerservice.repository.CustomerAddressRepository;
import com.cfs.customerservice.repository.CustomerDocumentRepository;
import com.cfs.customerservice.repository.CustomerRepository;
import com.cfs.customerservice.repository.EmploymentDetailRepository;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerAddressRepository addressRepository;

    private final EmploymentDetailRepository employmentRepository;

    private final BankAccountRepository bankAccountRepository;

    private final CustomerDocumentRepository documentRepository;

    private final KycReadinessEvaluator kycReadinessEvaluator;

    private final AdminKycService adminKycService;
    private final CustomerOwnership ownership;


    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerAddressRepository addressRepository,
            EmploymentDetailRepository employmentRepository,
            BankAccountRepository bankAccountRepository,
            CustomerDocumentRepository documentRepository,
            KycReadinessEvaluator kycReadinessEvaluator,
            AdminKycService adminKycService,
            CustomerOwnership ownership) {

        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.employmentRepository = employmentRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.documentRepository = documentRepository;
        this.kycReadinessEvaluator = kycReadinessEvaluator;
        this.adminKycService = adminKycService;
        this.ownership = ownership;
    }


    // =====================================================
    // CREATE CUSTOMER
    // =====================================================

    @Override
    public CustomerResponse createCustomer(
            CustomerRequest request) {

        ownership.assertUserId(request.getUserId());

        if (customerRepository
                .existsByUserId(request.getUserId())) {

            throw new DuplicateResourceException(
                    "Customer profile already exists for user ID: "
                            + request.getUserId()
            );
        }

        if (customerRepository
                .existsByMobileNumber(
                        request.getMobileNumber())) {

            throw new DuplicateResourceException(
                    "Mobile number already registered: "
                            + request.getMobileNumber()
            );
        }

        if (request.getPanNumber() != null
                && !request.getPanNumber().isBlank()
                && customerRepository.existsByPanNumber(
                        request.getPanNumber()
                                .trim()
                                .toUpperCase())) {

            throw new DuplicateResourceException(
                    "PAN number already registered"
            );
        }

        Customer customer = new Customer();

        customer.setUserId(
                request.getUserId()
        );

        customer.setCustomerNumber(
                generateCustomerNumber()
        );

        customer.setFirstName(
                request.getFirstName().trim()
        );

        if (request.getLastName() != null) {
            customer.setLastName(
                    request.getLastName().trim()
            );
        }

        customer.setDateOfBirth(
                request.getDateOfBirth()
        );

        customer.setMobileNumber(
                request.getMobileNumber().trim()
        );

        if (request.getPanNumber() != null
                && !request.getPanNumber().isBlank()) {

            customer.setPanNumber(
                    request.getPanNumber()
                            .trim()
                            .toUpperCase()
            );
        }

        customer.setKycStatus("PENDING");

        customer.setOnboardingStatus(
                "PROFILE_CREATED"
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        return mapCustomer(savedCustomer);
    }


    // =====================================================
    // GET CUSTOMER BY CUSTOMER ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(
            Long customerId) {

        Customer customer =
                findCustomer(customerId);
        ownership.assertOwned(customer);

        return mapCustomer(customer);
    }


    // =====================================================
    // GET CUSTOMER BY AUTH USER ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(
            Long userId) {

        ownership.assertUserId(userId);

        Customer customer =
                customerRepository
                        .findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found for user ID: "
                                                + userId
                                )
                        );

        return mapCustomer(customer);
    }


    // =====================================================
    // ADDRESS
    // =====================================================

    @Override
    public AddressResponse addAddress(
            Long customerId,
            AddressRequest request) {

        Customer customer =
                findCustomerForUpdate(customerId);
        ownership.assertOwned(customer);

        String addressType =
                request.getAddressType()
                        .trim()
                        .toUpperCase();

        Set<String> allowedTypes =
                Set.of(
                        "CURRENT",
                        "PERMANENT"
                );

        if (!allowedTypes.contains(addressType)) {

            throw new IllegalArgumentException(
                    "Address type must be CURRENT or PERMANENT"
            );
        }

        if (addressRepository
                .existsByCustomerIdAndAddressType(
                        customerId,
                        addressType)) {

            throw new DuplicateResourceException(
                    addressType
                            + " address already exists"
            );
        }

        CustomerAddress address =
                new CustomerAddress();

        address.setCustomerId(customerId);
        address.setAddressType(addressType);

        address.setAddressLine1(
                request.getAddressLine1()
                        .trim()
        );

        if (request.getAddressLine2() != null) {

            address.setAddressLine2(
                    request.getAddressLine2()
                            .trim()
            );
        }

        address.setCity(
                request.getCity().trim()
        );

        address.setState(
                request.getState().trim()
        );

        address.setPincode(
                request.getPincode().trim()
        );

        address.setCountry(
                request.getCountry().trim()
        );

        CustomerAddress saved =
                addressRepository.save(address);

        updateOnboardingStatus(customer);

        return mapAddress(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(
            Long customerId) {

        ownership.assertOwned(findCustomer(customerId));

        return addressRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::mapAddress)
                .toList();
    }


    // =====================================================
    // EMPLOYMENT
    // =====================================================

    @Override
    public EmploymentResponse addEmployment(
            Long customerId,
            EmploymentRequest request) {

        Customer customer =
                findCustomerForUpdate(customerId);
        ownership.assertOwned(customer);

        if (employmentRepository
                .existsByCustomerId(customerId)) {

            throw new DuplicateResourceException(
                    "Employment details already exist"
            );
        }

        EmploymentDetail employment =
                new EmploymentDetail();

        employment.setCustomerId(customerId);

        employment.setEmploymentType(
                request.getEmploymentType()
                        .trim()
                        .toUpperCase()
        );

        employment.setEmployerName(
                request.getEmployerName()
        );

        employment.setDesignation(
                request.getDesignation()
        );

        employment.setMonthlyIncome(
                request.getMonthlyIncome()
        );

        employment.setExistingMonthlyEmi(
                request.getExistingMonthlyEmi()
        );

        employment.setWorkExperienceMonths(
                request.getWorkExperienceMonths()
        );

        EmploymentDetail saved =
                employmentRepository.save(
                        employment
                );

        updateOnboardingStatus(customer);

        return mapEmployment(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public EmploymentResponse getEmployment(
            Long customerId) {

        ownership.assertOwned(findCustomer(customerId));

        EmploymentDetail employment =
                employmentRepository
                        .findByCustomerId(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employment details not found for customer: "
                                                + customerId
                                )
                        );

        return mapEmployment(employment);
    }


    // =====================================================
    // BANK ACCOUNT
    // =====================================================

    @Override
    public BankAccountResponse addBankAccount(
            Long customerId,
            BankAccountRequest request) {

        Customer customer =
                findCustomerForUpdate(customerId);
        ownership.assertOwned(customer);

        BankAccount account =
                new BankAccount();

        account.setCustomerId(customerId);

        account.setAccountHolderName(
                request.getAccountHolderName()
                        .trim()
        );

        account.setBankName(
                request.getBankName().trim()
        );

        account.setAccountNumber(
                request.getAccountNumber()
                        .trim()
        );

        account.setIfscCode(
                request.getIfscCode()
                        .trim()
                        .toUpperCase()
        );

        account.setAccountType(
                request.getAccountType()
                        .trim()
                        .toUpperCase()
        );

        account.setPrimaryAccount(
                request.isPrimaryAccount()
        );

        BankAccount saved =
                bankAccountRepository.save(
                        account
                );

        updateOnboardingStatus(customer);

        return mapBankAccount(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse>
            getBankAccounts(Long customerId) {

        ownership.assertOwned(findCustomer(customerId));

        return bankAccountRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::mapBankAccount)
                .toList();
    }


    // =====================================================
    // DOCUMENTS
    // =====================================================

    @Override
    public DocumentResponse addDocument(
            Long customerId,
            DocumentRequest request) {

        Customer customer =
                findCustomerForUpdate(customerId);
        ownership.assertOwned(customer);

        String documentType =
                request.getDocumentType()
                        .trim()
                        .toUpperCase();

        Set<String> allowedDocumentTypes =
                Set.of(
                        "PAN",
                        "IDENTITY_PROOF",
                        "ADDRESS_PROOF",
                        "SALARY_SLIP",
                        "BANK_STATEMENT"
                );

        if (!allowedDocumentTypes.contains(
                documentType)) {

            throw new IllegalArgumentException(
                    "Invalid document type"
            );
        }

        /*
         * PAN, identity and address proofs should only
         * occur once in this simple project.
         *
         * Salary slips and bank statements might later
         * need multiple files, so don't enforce one
         * document for those here.
         */
        if (
                Set.of(
                        "PAN",
                        "IDENTITY_PROOF",
                        "ADDRESS_PROOF"
                ).contains(documentType)
                &&
                documentRepository
                    .existsByCustomerIdAndDocumentType(
                            customerId,
                            documentType)) {

            throw new DuplicateResourceException(
                    documentType
                            + " document already exists"
            );
        }

        CustomerDocument document =
                new CustomerDocument();

        document.setCustomerId(
                customerId
        );

        document.setDocumentType(
                documentType
        );

        document.setFileName(
                request.getFileName()
                        .trim()
        );

        document.setFilePath(
                request.getFilePath()
                        .trim()
        );

        document.setVerificationStatus(
                "UPLOADED"
        );

        document.setUploadedAt(
                LocalDateTime.now()
        );

        CustomerDocument saved =
                documentRepository.save(
                        document
                );

        updateOnboardingStatus(customer);

        return mapDocument(saved);
    }


    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(
            Long customerId) {

        ownership.assertOwned(findCustomer(customerId));

        return documentRepository
                .findByCustomerId(customerId)
                .stream()
                .map(this::mapDocument)
                .toList();
    }


    // =====================================================
    // KYC
    // =====================================================

    /*
     * Generic ADMIN PATCH. Only the admin decisions (VERIFIED / REJECTED)
     * are accepted and they are delegated to AdminKycService so the
     * UNDER_REVIEW guard, readiness check, document verification and
     * onboarding recalculation are identical to the dedicated admin routes.
     */
    @Override
    public CustomerResponse updateKycStatus(
            Long customerId,
            KycStatusRequest request) {

        Customer customer =
                findCustomerForUpdate(customerId);

        String status =
                request.getStatus()
                        .trim()
                        .toUpperCase();

        Set<String> allowedStatuses =
                Set.of(
                        "PENDING",
                        "UNDER_REVIEW",
                        "VERIFIED",
                        "REJECTED"
                );

        if (!allowedStatuses.contains(status)) {

            throw new WorkflowConflictException(
                    "Invalid KYC status"
            );
        }

        switch (status) {

            case "VERIFIED" ->
                    adminKycService.verifyKyc(customerId);

            case "REJECTED" -> {

                KycRejectRequest rejectRequest =
                        new KycRejectRequest();

                rejectRequest.setReason(
                        request.getRemarks()
                );

                adminKycService.rejectKyc(
                        customerId,
                        rejectRequest
                );
            }

            default ->
                    throw new WorkflowConflictException(
                            "KYC status cannot be changed to "
                                    + status
                                    + " by an administrator; only VERIFIED or REJECTED decisions on UNDER_REVIEW KYC are allowed"
                    );
        }

        // Same managed instance (same persistence context), so it reflects the decision.
        return mapCustomer(customer);
    }


    // =====================================================
    // KYC REVIEW SUBMISSION (CUSTOMER)
    // =====================================================

    @Override
    public CustomerResponse requestKycReview(
            Long customerId) {

        Customer customer =
                findCustomerForUpdate(customerId);

        ownership.assertOwned(customer);

        String currentStatus =
                customer.getKycStatus() == null
                        ? ""
                        : customer.getKycStatus()
                                .trim()
                                .toUpperCase(Locale.ROOT);

        switch (currentStatus) {

            case "UNDER_REVIEW":
                // Idempotent repeat: no save, no timestamp or document changes.
                return mapCustomer(customer);

            case "PENDING":
            case "REJECTED":
                break;

            case "VERIFIED":
                throw new WorkflowConflictException(
                        "KYC is already VERIFIED and cannot be resubmitted for review"
                );

            default:
                throw new WorkflowConflictException(
                        "KYC review cannot be requested from status "
                                + customer.getKycStatus()
                );
        }

        KycReadinessEvaluator.Readiness readiness =
                kycReadinessEvaluator.evaluate(customer);

        kycReadinessEvaluator.assertReady(readiness);

        customer.setKycStatus("UNDER_REVIEW");

        customer.setOnboardingStatus(
                kycReadinessEvaluator.resolveOnboardingStatus(
                        customer.getKycStatus(),
                        readiness
                )
        );

        Customer saved =
                customerRepository.save(customer);

        return mapCustomer(saved);
    }


    // =====================================================
    // ONBOARDING STATUS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public OnboardingStatusResponse
            getOnboardingStatus(
            Long customerId) {

        Customer customer =
                findCustomer(customerId);
        ownership.assertOwned(customer);

        KycReadinessEvaluator.Readiness readiness =
                kycReadinessEvaluator.evaluate(customer);

        return new OnboardingStatusResponse(
                customerId,
                readiness.isProfileCompleted(),
                readiness.isAddressCompleted(),
                readiness.isEmploymentCompleted(),
                readiness.isBankCompleted(),
                readiness.isDocumentsCompleted(),
                customer.getKycStatus(),
                kycReadinessEvaluator.resolveOnboardingStatus(
                        customer.getKycStatus(),
                        readiness
                )
        );
    }


    // =====================================================
    // INTERNAL ONBOARDING STATUS UPDATE
    // =====================================================

    private void updateOnboardingStatus(
            Customer customer) {

        customer.setOnboardingStatus(
                kycReadinessEvaluator.resolveOnboardingStatus(
                        customer.getKycStatus(),
                        kycReadinessEvaluator.evaluate(customer)
                )
        );

        customerRepository.save(customer);
    }


    // =====================================================
    // FIND CUSTOMER
    // =====================================================

    private Customer findCustomer(
            Long customerId) {

        return customerRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: "
                                        + customerId
                        )
                );
    }


    private Customer findCustomerForUpdate(
            Long customerId) {

        return customerRepository
                .findByIdForUpdate(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: "
                                        + customerId
                        )
                );
    }


    // =====================================================
    // CUSTOMER NUMBER
    // =====================================================

    private String generateCustomerNumber() {

        return "CFS"
                + System.currentTimeMillis();
    }


    // =====================================================
    // CUSTOMER MAPPER
    // =====================================================

    private CustomerResponse mapCustomer(
            Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getUserId(),
                customer.getCustomerNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDateOfBirth(),
                customer.getMobileNumber(),
                customer.getPanNumber(),
                customer.getKycStatus(),
                customer.getOnboardingStatus()
        );
    }


    // =====================================================
    // ADDRESS MAPPER
    // =====================================================

    private AddressResponse mapAddress(
            CustomerAddress address) {

        return new AddressResponse(
                address.getId(),
                address.getCustomerId(),
                address.getAddressType(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPincode(),
                address.getCountry()
        );
    }


    // =====================================================
    // EMPLOYMENT MAPPER
    // =====================================================

    private EmploymentResponse mapEmployment(
            EmploymentDetail employment) {

        return new EmploymentResponse(
                employment.getId(),
                employment.getCustomerId(),
                employment.getEmploymentType(),
                employment.getEmployerName(),
                employment.getDesignation(),
                employment.getMonthlyIncome(),
                employment.getExistingMonthlyEmi(),
                employment.getWorkExperienceMonths()
        );
    }


    // =====================================================
    // BANK MAPPER
    // =====================================================

    private BankAccountResponse mapBankAccount(
            BankAccount account) {

        return new BankAccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getAccountHolderName(),
                account.getBankName(),
                account.getAccountNumber(),
                account.getIfscCode(),
                account.getAccountType(),
                account.isPrimaryAccount()
        );
    }


    // =====================================================
    // DOCUMENT MAPPER
    // =====================================================

    private DocumentResponse mapDocument(
            CustomerDocument document) {

        return new DocumentResponse(
                document.getId(),
                document.getCustomerId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getFilePath(),
                document.getVerificationStatus(),
                document.getUploadedAt()
        );
    }
}
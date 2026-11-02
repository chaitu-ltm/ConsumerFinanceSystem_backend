package com.cfs.disbursement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.disbursement.client.ApprovalClient;
import com.cfs.disbursement.dto.request.DisbursementRequest;
import com.cfs.disbursement.dto.response.ApprovalResponse;
import com.cfs.disbursement.dto.response.DisbursementResponse;
import com.cfs.disbursement.entity.Disbursement;
import com.cfs.disbursement.entity.DisbursementStatus;
import com.cfs.disbursement.exception.DisbursementNotFoundException;
import com.cfs.disbursement.exception.DuplicateDisbursementException;
import com.cfs.disbursement.exception.LoanNotApprovedException;
import com.cfs.disbursement.exception.ApprovalUnavailableException;
import com.cfs.disbursement.mapper.DisbursementMapper;
import com.cfs.disbursement.repository.DisbursementRepository;
import com.cfs.disbursement.service.DisbursementService;

@Service
@Transactional
public class DisbursementServiceImpl
        implements DisbursementService {

    private final DisbursementRepository repository;
    private final DisbursementMapper mapper;
    private final ApprovalClient approvalClient;

    public DisbursementServiceImpl(
            DisbursementRepository repository,
            DisbursementMapper mapper,
            ApprovalClient approvalClient) {

        this.repository = repository;
        this.mapper = mapper;
        this.approvalClient = approvalClient;
    }

    @Override
    public DisbursementResponse createDisbursement(
            DisbursementRequest request,
            String processedBy) {

        if (repository.existsByApplicationId(
                request.getApplicationId())) {

            throw new DuplicateDisbursementException(
                    "Disbursement already exists for application ID: "
                            + request.getApplicationId()
            );
        }

        ApprovalResponse approval;
        try {
            approval = approvalClient.getApprovalByApplicationId(request.getApplicationId());
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new LoanNotApprovedException(
                        "Approval decision not found for application ID: " + request.getApplicationId());
            }
            throw new ApprovalUnavailableException(ex);
        }
        if (approval == null || approval.getApprovalId() == null
                || approval.getApprovalId() <= 0) {
            throw new LoanNotApprovedException(
                    "Approval response must contain a valid approval ID"
            );
        }

        if (approval.getApplicationId() == null
                || !approval.getApplicationId().equals(request.getApplicationId())) {

            throw new LoanNotApprovedException(
                    "Approval does not match application ID: "
                            + request.getApplicationId()
            );
        }

        if (!"APPROVED".equals(approval.getDecision())) {
            throw new LoanNotApprovedException(
                    "Application ID " + request.getApplicationId() + " is not approved");
        }

        Disbursement entity =
                new Disbursement();

        entity.setApplicationId(
                request.getApplicationId()
        );

        entity.setApprovalId(
                approval.getApprovalId()
        );

        entity.setAmount(
                request.getAmount()
        );

        entity.setStatus(
                DisbursementStatus.DISBURSED
        );

        entity.setProcessedBy(
                processedBy
        );

        entity.setTransactionReference(
                "CFSDIS-"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase()
        );

        entity.setDisbursedAt(
                LocalDateTime.now()
        );

        Disbursement saved =
                repository.save(entity);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DisbursementResponse getById(
            Long disbursementId) {

        Disbursement entity =
                repository.findById(disbursementId)
                        .orElseThrow(() ->
                                new DisbursementNotFoundException(
                                        "Disbursement not found with ID: "
                                                + disbursementId
                                )
                        );

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DisbursementResponse getByApplicationId(
            Long applicationId) {

        Disbursement entity =
                repository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new DisbursementNotFoundException(
                                        "Disbursement not found for application ID: "
                                                + applicationId
                                )
                        );

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisbursementResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
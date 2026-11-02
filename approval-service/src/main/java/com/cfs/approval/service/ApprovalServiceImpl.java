package com.cfs.approval.service;

import java.util.List;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.approval.dto.request.ApprovalDecisionRequest;
import com.cfs.approval.dto.response.ApprovalDecisionResponse;
import com.cfs.approval.client.CreditAssessmentClient;
import com.cfs.approval.dto.response.CreditAssessmentResponse;
import com.cfs.approval.dto.response.CreditAssessmentResponse.AssessmentStatus;
import com.cfs.approval.exception.InvalidAssessmentException;
import com.cfs.approval.exception.CreditAssessmentUnavailableException;
import com.cfs.approval.entity.ApprovalDecision;
import com.cfs.approval.entity.ApprovalStatus;
import com.cfs.approval.exception.ApprovalNotFoundException;
import com.cfs.approval.exception.DuplicateApprovalException;
import com.cfs.approval.mapper.ApprovalMapper;
import com.cfs.approval.repository.ApprovalRepository;
import com.cfs.approval.service.ApprovalService;

@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final ApprovalMapper approvalMapper;
    private final CreditAssessmentClient creditAssessmentClient;

    public ApprovalServiceImpl(
            ApprovalRepository approvalRepository,
            ApprovalMapper approvalMapper,
            CreditAssessmentClient creditAssessmentClient) {

        this.approvalRepository = approvalRepository;
        this.approvalMapper = approvalMapper;
        this.creditAssessmentClient = creditAssessmentClient;
    }

    @Override
    public ApprovalDecisionResponse createDecision(
            ApprovalDecisionRequest request) {

        boolean decisionExists =
                approvalRepository.existsByApplicationId(
                        request.getApplicationId()
                );

        if (decisionExists) {
            throw new DuplicateApprovalException(
                    "Approval decision already exists for application ID: "
                            + request.getApplicationId()
            );
        }

        CreditAssessmentResponse assessment;
        try {
            assessment = creditAssessmentClient.getById(request.getCreditAssessmentId());
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new InvalidAssessmentException(
                        "Credit assessment not found with ID: " + request.getCreditAssessmentId());
            }
            throw new CreditAssessmentUnavailableException(ex);
        }

        if (assessment == null || assessment.getCreditAssessmentId() == null
                || !assessment.getCreditAssessmentId().equals(request.getCreditAssessmentId())) {
            throw new InvalidAssessmentException(
                    "Credit assessment response does not match requested assessment ID: "
                            + request.getCreditAssessmentId());
        }
        if (assessment.getApplicationId() == null
                || !assessment.getApplicationId().equals(request.getApplicationId())) {
            throw new InvalidAssessmentException(
                    "Credit assessment does not match application ID: " + request.getApplicationId());
        }
        if (request.getDecision() == ApprovalStatus.APPROVED
                && assessment.getStatus() != AssessmentStatus.ELIGIBLE) {
            throw new InvalidAssessmentException(
                    "Only an ELIGIBLE credit assessment can be approved");
        }

        ApprovalDecision approval =
                approvalMapper.toEntity(request);

        ApprovalDecision savedApproval =
                approvalRepository.save(approval);

        return approvalMapper.toResponse(savedApproval);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalDecisionResponse getDecisionById(
            Long approvalId) {

        ApprovalDecision approval =
                approvalRepository.findById(approvalId)
                        .orElseThrow(() ->
                                new ApprovalNotFoundException(
                                        "Approval decision not found with ID: "
                                                + approvalId
                                )
                        );

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalDecisionResponse getDecisionByApplicationId(
            Long applicationId) {

        ApprovalDecision approval =
                approvalRepository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new ApprovalNotFoundException(
                                        "Approval decision not found for application ID: "
                                                + applicationId
                                )
                        );

        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalDecisionResponse> getAllDecisions() {

        return approvalRepository.findAll()
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalDecisionResponse> getDecisionsByStatus(
            ApprovalStatus status) {

        return approvalRepository.findByDecision(status)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalDecisionResponse> getDecisionsByReviewer(
            String reviewedBy) {

        return approvalRepository
                .findByReviewedByIgnoreCase(reviewedBy)
                .stream()
                .map(approvalMapper::toResponse)
                .toList();
    }
}
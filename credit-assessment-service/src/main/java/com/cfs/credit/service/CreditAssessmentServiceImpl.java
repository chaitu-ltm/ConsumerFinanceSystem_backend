package com.cfs.credit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfs.credit.dto.request.CreditAssessmentRequest;
import com.cfs.credit.dto.response.CreditAssessmentResponse;
import com.cfs.credit.entity.AssessmentStatus;
import com.cfs.credit.entity.CreditAssessment;
import com.cfs.credit.exception.AssessmentNotFoundException;
import com.cfs.credit.exception.DuplicateAssessmentException;
import com.cfs.credit.mapper.CreditAssessmentMapper;
import com.cfs.credit.repository.CreditAssessmentRepository;
import com.cfs.credit.client.FinanceApplicationClient;
import com.cfs.credit.dto.response.ApplicationAssessmentContextResponse;
import com.cfs.credit.exception.ApplicationVerificationException;
import com.cfs.credit.exception.ApplicationVerificationException.Failure;
import feign.FeignException;
import feign.codec.DecodeException;

@Service
@Transactional
public class CreditAssessmentServiceImpl
        implements CreditAssessmentService {

    private final CreditAssessmentRepository repository;
    private final CreditAssessmentMapper mapper;
    private final FinanceApplicationClient financeApplicationClient;

    public CreditAssessmentServiceImpl(
            CreditAssessmentRepository repository,
            CreditAssessmentMapper mapper,
            FinanceApplicationClient financeApplicationClient) {

        this.repository = repository;
        this.mapper = mapper;
        this.financeApplicationClient = financeApplicationClient;
    }

    @Override
    public CreditAssessmentResponse createAssessment(
            CreditAssessmentRequest request,
            String assessedBy) {

        if (repository.existsByApplicationId(
                request.getApplicationId())) {

            throw new DuplicateAssessmentException(
                    "Credit assessment already exists "
                            + "for application ID: "
                            + request.getApplicationId()
            );
        }

        verifyApplication(request.getApplicationId());

        CreditAssessment entity =
                mapper.toEntity(
                        request,
                        assessedBy
                );

        CreditAssessment saved =
                repository.save(entity);

        return mapper.toResponse(saved);
    }

    private void verifyApplication(Long applicationId) {
        ApplicationAssessmentContextResponse context;
        try {
            context = financeApplicationClient.getAssessmentContext(applicationId);
        } catch (DecodeException exception) {
            throw new ApplicationVerificationException(Failure.INVALID_CONTEXT);
        } catch (FeignException exception) {
            Failure failure = switch (exception.status()) {
                case 404 -> Failure.NOT_FOUND;
                case 401 -> Failure.UNAUTHORIZED;
                case 403 -> Failure.FORBIDDEN;
                default -> exception.status() < 0 || exception.status() >= 500
                        ? Failure.UNAVAILABLE : Failure.INVALID_CONTEXT;
            };
            throw new ApplicationVerificationException(failure);
        }

        if (context == null || context.getApplicationId() == null
                || !applicationId.equals(context.getApplicationId()) || context.getStatus() == null) {
            throw new ApplicationVerificationException(Failure.INVALID_CONTEXT);
        }
        if (!"SUBMITTED".equals(context.getStatus())) {
            throw new ApplicationVerificationException(Failure.INVALID_STATE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CreditAssessmentResponse getById(
            Long creditAssessmentId) {

        CreditAssessment entity =
                repository.findById(creditAssessmentId)
                        .orElseThrow(() ->
                                new AssessmentNotFoundException(
                                        "Credit assessment not found "
                                                + "with ID: "
                                                + creditAssessmentId
                                )
                        );

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditAssessmentResponse getByApplicationId(
            Long applicationId) {

        CreditAssessment entity =
                repository
                        .findByApplicationId(applicationId)
                        .orElseThrow(() ->
                                new AssessmentNotFoundException(
                                        "Credit assessment not found "
                                                + "for application ID: "
                                                + applicationId
                                )
                        );

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditAssessmentResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreditAssessmentResponse> getByStatus(
            AssessmentStatus status) {

        return repository.findByStatus(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
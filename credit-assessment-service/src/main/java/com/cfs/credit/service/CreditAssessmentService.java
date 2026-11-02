package com.cfs.credit.service;

import java.util.List;

import com.cfs.credit.dto.request.CreditAssessmentRequest;
import com.cfs.credit.dto.response.CreditAssessmentResponse;
import com.cfs.credit.entity.AssessmentStatus;

public interface CreditAssessmentService {

    CreditAssessmentResponse createAssessment(
            CreditAssessmentRequest request,
            String assessedBy
    );

    CreditAssessmentResponse getById(
            Long creditAssessmentId
    );

    CreditAssessmentResponse getByApplicationId(
            Long applicationId
    );

    List<CreditAssessmentResponse> getAll();

    List<CreditAssessmentResponse> getByStatus(
            AssessmentStatus status
    );
}
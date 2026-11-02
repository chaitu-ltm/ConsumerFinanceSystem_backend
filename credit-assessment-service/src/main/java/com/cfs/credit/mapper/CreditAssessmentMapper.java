package com.cfs.credit.mapper;

import org.springframework.stereotype.Component;

import com.cfs.credit.dto.request.CreditAssessmentRequest;
import com.cfs.credit.dto.response.CreditAssessmentResponse;
import com.cfs.credit.entity.CreditAssessment;

@Component
public class CreditAssessmentMapper {

    public CreditAssessment toEntity(
            CreditAssessmentRequest request,
            String assessedBy) {

        CreditAssessment entity =
                new CreditAssessment();

        entity.setApplicationId(
                request.getApplicationId()
        );

        entity.setStatus(
                request.getStatus()
        );

        entity.setReason(
                request.getReason().trim()
        );

        entity.setAssessedBy(
                assessedBy
        );

        return entity;
    }

    public CreditAssessmentResponse toResponse(
            CreditAssessment entity) {

        CreditAssessmentResponse response =
                new CreditAssessmentResponse();

        response.setCreditAssessmentId(
                entity.getCreditAssessmentId()
        );

        response.setApplicationId(
                entity.getApplicationId()
        );

        response.setStatus(
                entity.getStatus()
        );

        response.setReason(
                entity.getReason()
        );

        response.setAssessedBy(
                entity.getAssessedBy()
        );

        response.setAssessedAt(
                entity.getAssessedAt()
        );

        return response;
    }
}
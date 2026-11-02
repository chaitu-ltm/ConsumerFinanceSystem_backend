package com.cfs.disbursement.mapper;

import org.springframework.stereotype.Component;

import com.cfs.disbursement.dto.response.DisbursementResponse;
import com.cfs.disbursement.entity.Disbursement;

@Component
public class DisbursementMapper {

    public DisbursementResponse toResponse(
            Disbursement entity) {

        DisbursementResponse response =
                new DisbursementResponse();

        response.setDisbursementId(
                entity.getDisbursementId()
        );

        response.setApplicationId(
                entity.getApplicationId()
        );

        response.setApprovalId(
                entity.getApprovalId()
        );

        response.setAmount(
                entity.getAmount()
        );

        response.setStatus(
                entity.getStatus()
        );

        response.setTransactionReference(
                entity.getTransactionReference()
        );

        response.setProcessedBy(
                entity.getProcessedBy()
        );

        response.setDisbursedAt(
                entity.getDisbursedAt()
        );

        return response;
    }
}
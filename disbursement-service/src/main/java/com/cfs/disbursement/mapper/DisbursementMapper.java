package com.cfs.disbursement.mapper;

import org.springframework.stereotype.Component;

import com.cfs.disbursement.dto.response.DisbursementResponse;
import com.cfs.disbursement.entity.Disbursement;

@Component
public class DisbursementMapper {

    public DisbursementResponse toResponse(
            Disbursement entity) {

        return new DisbursementResponse(
                entity.getDisbursementId(),
                entity.getApplicationId(),
                entity.getApprovalId(),
                entity.getAmount(),
                entity.getStatus(),
                entity.getTransactionReference(),
                entity.getProcessedBy(),
                entity.getDisbursedAt());
    }
}
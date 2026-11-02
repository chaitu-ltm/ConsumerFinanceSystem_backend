package com.cfs.disbursement.service;

import java.util.List;

import com.cfs.disbursement.dto.request.DisbursementRequest;
import com.cfs.disbursement.dto.response.DisbursementResponse;

public interface DisbursementService {

    DisbursementResponse createDisbursement(
            DisbursementRequest request,
            String processedBy
    );

    DisbursementResponse getById(
            Long disbursementId
    );

    DisbursementResponse getByApplicationId(
            Long applicationId
    );

    List<DisbursementResponse> getAll();
}
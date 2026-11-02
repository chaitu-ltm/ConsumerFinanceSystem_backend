package com.cfs.applicationservice.service;

import java.util.List;

import com.cfs.applicationservice.dto.ApplicationAssessmentContextResponse;
import com.cfs.applicationservice.dto.FinanceApplicationRequest;
import com.cfs.applicationservice.dto.FinanceApplicationResponse;
import com.cfs.applicationservice.dto.StatusHistoryResponse;

public interface FinanceApplicationService {

    /**
     * Reads only the application ID and current status, without product or history access.
     */
    ApplicationAssessmentContextResponse getAssessmentContext(Long applicationId);

    FinanceApplicationResponse createApplication(
            FinanceApplicationRequest request
    );

    FinanceApplicationResponse getApplication(
            Long applicationId
    );

    List<FinanceApplicationResponse>
            getApplicationsByCustomer(
            Long customerId
    );

    FinanceApplicationResponse submitApplication(
            Long applicationId
    );

    List<StatusHistoryResponse> getStatusHistory(
            Long applicationId
    );
}
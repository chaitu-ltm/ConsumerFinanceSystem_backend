package com.cfs.credit.client;

import com.cfs.credit.config.FinanceApplicationFeignConfig;
import com.cfs.credit.dto.response.ApplicationAssessmentContextResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "finance-application-service", configuration = FinanceApplicationFeignConfig.class)
public interface FinanceApplicationClient {

    @GetMapping("/api/applications/{applicationId}/assessment-context")
    ApplicationAssessmentContextResponse getAssessmentContext(
            @PathVariable("applicationId") Long applicationId);
}

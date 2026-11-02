package com.cfs.approval.client;

import com.cfs.approval.config.CreditAssessmentFeignConfig;
import com.cfs.approval.dto.response.CreditAssessmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "credit-assessment-service",
        configuration = CreditAssessmentFeignConfig.class)
public interface CreditAssessmentClient {

    @GetMapping("/api/credit-assessments/{creditAssessmentId}")
    CreditAssessmentResponse getById(
            @PathVariable("creditAssessmentId") Long creditAssessmentId);
}

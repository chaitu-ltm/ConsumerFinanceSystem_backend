package com.cfs.disbursement.client;

import com.cfs.disbursement.config.FeignConfig;
import com.cfs.disbursement.dto.response.ApprovalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "approval-service",
        configuration = FeignConfig.class)
public interface ApprovalClient {

    @GetMapping(
            "/api/approvals/application/{applicationId}"
    )
    ApprovalResponse getApprovalByApplicationId(
            @PathVariable("applicationId")
            Long applicationId
    );
}
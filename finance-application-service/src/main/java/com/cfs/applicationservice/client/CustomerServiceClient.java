package com.cfs.applicationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cfs.applicationservice.dto
        .CustomerOnboardingResponse;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping(
        "/api/customers/{customerId}/onboarding-status"
    )
    CustomerOnboardingResponse getOnboardingStatus(
            @PathVariable("customerId")
            Long customerId
    );
}
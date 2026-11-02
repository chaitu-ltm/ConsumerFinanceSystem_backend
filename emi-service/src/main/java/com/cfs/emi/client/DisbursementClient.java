package com.cfs.emi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cfs.emi.dto.DisbursementResponse;

@FeignClient(name = "disbursement-service", configuration = DisbursementFeignConfig.class)
public interface DisbursementClient {
    @GetMapping("/api/disbursements/{disbursementId}")
    DisbursementResponse getById(@PathVariable("disbursementId") Long disbursementId);
}

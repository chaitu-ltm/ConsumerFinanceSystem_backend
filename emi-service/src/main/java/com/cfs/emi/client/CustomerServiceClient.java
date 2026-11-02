package com.cfs.emi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cfs.emi.dto.CustomerIdentityResponse;

@FeignClient(name = "customer-service", configuration = CustomerIdentityFeignConfig.class)
public interface CustomerServiceClient {
    @GetMapping("/api/customers/user/{userId}")
    CustomerIdentityResponse getByUserId(@PathVariable("userId") Long userId);
}

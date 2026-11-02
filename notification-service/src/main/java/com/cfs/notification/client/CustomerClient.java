package com.cfs.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", contextId = "notificationCustomer",
        configuration = CustomerClientConfiguration.class)
public interface CustomerClient {

    @GetMapping("/api/customers/user/{userId}")
    CustomerIdentityResponse getCustomerByUserId(@PathVariable("userId") Long userId);
}

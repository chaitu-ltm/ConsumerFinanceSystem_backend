package com.cfs.notification.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.cfs.notification.client.CustomerClient;
import com.cfs.notification.client.CustomerIdentityResponse;
import com.cfs.notification.exception.CustomerLookupUnavailableException;

import feign.FeignException;

@Component
public class NotificationOwnership {

    private final CustomerClient customerClient;

    public NotificationOwnership(CustomerClient customerClient) {
        this.customerClient = customerClient;
    }

    public void requireCustomer(Long customerId) {
        var jwt = NotificationIdentity.current();
        if (NotificationIdentity.hasRole(jwt, "ROLE_ADMIN")) {
            return;
        }
        if (!NotificationIdentity.hasRole(jwt, "ROLE_CUSTOMER") || jwt.getUserId() == null) {
            throw new AccessDeniedException("Access denied");
        }
        CustomerIdentityResponse customer;
        try {
            customer = customerClient.getCustomerByUserId(jwt.getUserId());
        } catch (FeignException exception) {
            if (exception.status() == 404 || exception.status() == 401 || exception.status() == 403) {
                throw new AccessDeniedException("Access denied");
            }
            throw new CustomerLookupUnavailableException(exception);
        } catch (RuntimeException exception) {
            throw new CustomerLookupUnavailableException(exception);
        }
        if (customer == null || customer.getUserId() == null || customer.getCustomerId() == null) {
            throw new CustomerLookupUnavailableException(null);
        }
        if (!jwt.getUserId().equals(customer.getUserId()) || customer.getCustomerId() <= 0
                || !customer.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}

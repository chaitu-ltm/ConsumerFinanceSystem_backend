package com.cfs.notification.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerIdentityResponse {

    private Long customerId;
    private Long userId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Object customerId) {
        this.customerId = exactId(customerId);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Object userId) {
        this.userId = exactId(userId);
    }

    private static Long exactId(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Invalid customer identity contract");
        }
        return new BigDecimal(value.toString()).longValueExact();
    }
}

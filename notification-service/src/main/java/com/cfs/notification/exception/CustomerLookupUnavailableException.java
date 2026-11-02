package com.cfs.notification.exception;

public class CustomerLookupUnavailableException extends RuntimeException {

    public CustomerLookupUnavailableException(Throwable cause) {
        super("Customer identity lookup unavailable", cause);
    }
}

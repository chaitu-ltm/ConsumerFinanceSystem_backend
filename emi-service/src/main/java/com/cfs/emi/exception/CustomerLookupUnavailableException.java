package com.cfs.emi.exception;

public class CustomerLookupUnavailableException extends RuntimeException {
    public CustomerLookupUnavailableException(Throwable cause) {
        super("Customer ownership verification is temporarily unavailable", cause);
    }
}

package com.cfs.applicationservice.exception;

public class CustomerServiceUnavailableException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CustomerServiceUnavailableException(
            String message) {

        super(message);
    }
}
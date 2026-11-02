package com.cfs.applicationservice.exception;

public class CustomerNotEligibleException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CustomerNotEligibleException(
            String message) {

        super(message);
    }
}
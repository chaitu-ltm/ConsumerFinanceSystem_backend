package com.cfs.applicationservice.exception;

public class InvalidApplicationStateException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidApplicationStateException(
            String message) {

        super(message);
    }
}
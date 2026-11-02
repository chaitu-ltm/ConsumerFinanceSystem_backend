package com.cfs.applicationservice.exception;

public class DuplicateResourceException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}
package com.cfs.disbursement.exception;

public class DuplicateDisbursementException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateDisbursementException(
            String message) {

        super(message);
    }
}
package com.cfs.disbursement.exception;

public class DisbursementNotFoundException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DisbursementNotFoundException(
            String message) {

        super(message);
    }
}
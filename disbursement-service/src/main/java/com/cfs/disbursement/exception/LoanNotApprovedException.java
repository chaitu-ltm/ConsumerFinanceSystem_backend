package com.cfs.disbursement.exception;

public class LoanNotApprovedException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LoanNotApprovedException(String message) {
        super(message);
    }
}
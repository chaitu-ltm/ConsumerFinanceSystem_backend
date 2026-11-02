package com.cfs.emi.exception;

public class DisbursementVerificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DisbursementVerificationException() {
        super("Disbursement details do not match the EMI schedule request");
    }
}

package com.cfs.emi.exception;

public class DisbursementLookupUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DisbursementLookupUnavailableException(Throwable cause) {
        super("Disbursement verification is temporarily unavailable", cause);
    }
}

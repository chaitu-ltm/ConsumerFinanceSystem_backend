package com.cfs.emi.exception;

public class DisbursementNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DisbursementNotFoundException() {
        super("Disbursement was not found");
    }
}

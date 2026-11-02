package com.cfs.emi.exception;

public class AlreadyPaidRepaymentException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AlreadyPaidRepaymentException() {
        super("Installment is already paid");
    }
}

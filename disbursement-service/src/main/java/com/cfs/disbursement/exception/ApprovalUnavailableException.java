package com.cfs.disbursement.exception;

public class ApprovalUnavailableException extends RuntimeException {
    public ApprovalUnavailableException(Throwable cause) {
        super("Unable to verify approval decision; please try again later", cause);
    }
}

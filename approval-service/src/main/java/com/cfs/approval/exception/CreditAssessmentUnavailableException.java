package com.cfs.approval.exception;

public class CreditAssessmentUnavailableException extends RuntimeException {
    public CreditAssessmentUnavailableException(Throwable cause) {
        super("Unable to verify credit assessment; please try again later", cause);
    }
}

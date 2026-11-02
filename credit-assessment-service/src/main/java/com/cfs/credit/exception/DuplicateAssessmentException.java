package com.cfs.credit.exception;

public class DuplicateAssessmentException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateAssessmentException(
            String message) {

        super(message);
    }
}
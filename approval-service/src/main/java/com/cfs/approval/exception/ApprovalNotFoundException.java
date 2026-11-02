package com.cfs.approval.exception;

public class ApprovalNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
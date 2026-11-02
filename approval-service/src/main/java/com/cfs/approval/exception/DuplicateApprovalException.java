package com.cfs.approval.exception;


public class DuplicateApprovalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateApprovalException(String message) {
        super(message);
    }
}
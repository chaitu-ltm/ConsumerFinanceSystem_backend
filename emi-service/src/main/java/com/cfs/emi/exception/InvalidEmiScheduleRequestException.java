package com.cfs.emi.exception;

public class InvalidEmiScheduleRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidEmiScheduleRequestException(String message) {
        super(message);
    }
}

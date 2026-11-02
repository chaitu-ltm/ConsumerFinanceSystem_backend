package com.cfs.credit.exception;

import org.springframework.http.HttpStatus;

public class ApplicationVerificationException extends RuntimeException {

    public enum Failure {
        NOT_FOUND(HttpStatus.NOT_FOUND, "Finance application was not found"),
        INVALID_STATE(HttpStatus.CONFLICT, "Only a SUBMITTED application can be assessed"),
        UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Application verification is temporarily unavailable"),
        INVALID_CONTEXT(HttpStatus.BAD_GATEWAY, "Application verification returned an invalid response"),
        UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Application verification requires valid authentication"),
        FORBIDDEN(HttpStatus.FORBIDDEN, "Application verification is not permitted");

        private final HttpStatus status;
        private final String message;

        Failure(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    private final Failure failure;

    public ApplicationVerificationException(Failure failure) {
        super(failure.message);
        this.failure = failure;
    }

    public HttpStatus getStatus() {
        return failure.status;
    }
}

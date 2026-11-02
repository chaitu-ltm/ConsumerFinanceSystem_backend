package com.cfs.emi.exception;

public class InvalidRepaymentException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidRepaymentException(String message) {
		super(message);
	}
}
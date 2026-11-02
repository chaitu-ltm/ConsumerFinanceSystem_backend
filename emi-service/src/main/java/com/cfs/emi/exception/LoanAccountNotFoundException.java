
package com.cfs.emi.exception;

public class LoanAccountNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LoanAccountNotFoundException(String message) {
		super(message);
	}
}

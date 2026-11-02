package com.cfs.emi.exception;

public class EmiInstallmentNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmiInstallmentNotFoundException(String message) {
		super(message);
	}
}

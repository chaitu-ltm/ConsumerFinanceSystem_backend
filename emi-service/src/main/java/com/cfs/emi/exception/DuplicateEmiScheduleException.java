package com.cfs.emi.exception;

public class DuplicateEmiScheduleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateEmiScheduleException(String message) {
		super(message);
	}
}
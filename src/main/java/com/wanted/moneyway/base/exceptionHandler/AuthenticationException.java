package com.wanted.moneyway.base.exceptionHandler;

public class AuthenticationException extends RuntimeException {
	public AuthenticationException(String message) {
		super(message);
	}
}
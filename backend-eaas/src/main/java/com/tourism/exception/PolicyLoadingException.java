package com.tourism.exception;

public class PolicyLoadingException extends RuntimeException {
    public PolicyLoadingException(String message) {
        super(message);
    }

    public PolicyLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}

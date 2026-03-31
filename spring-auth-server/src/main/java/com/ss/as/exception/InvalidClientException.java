package com.ss.as.exception;

/**
 * Exception thrown when OAuth2 client credentials are invalid.
 */
public class InvalidClientException extends RuntimeException {

    public InvalidClientException(String message) {
        super(message);
    }

    public InvalidClientException(String message, Throwable cause) {
        super(message, cause);
    }
}


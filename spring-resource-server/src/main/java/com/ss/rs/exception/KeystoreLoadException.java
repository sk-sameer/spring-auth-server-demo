package com.ss.rs.exception;

/**
 * Exception thrown when keystore cannot be loaded or is invalid.
 */
public class KeystoreLoadException extends Exception {

    public KeystoreLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeystoreLoadException(String message) {
        super(message);
    }

}

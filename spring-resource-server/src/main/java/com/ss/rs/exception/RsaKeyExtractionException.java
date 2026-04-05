package com.ss.rs.exception;

/**
 * Exception thrown when RSA key pair cannot be extracted from keystore.
 */
public class RsaKeyExtractionException extends Exception {

    public RsaKeyExtractionException(String message, Throwable cause) {
        super(message, cause);
    }

}

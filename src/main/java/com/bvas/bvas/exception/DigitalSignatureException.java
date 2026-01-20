package com.bvas.bvas.exception;

public class DigitalSignatureException extends RuntimeException {
    public DigitalSignatureException(String message) {
        super(message);
    }

    public DigitalSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
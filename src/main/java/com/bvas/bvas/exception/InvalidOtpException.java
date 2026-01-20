package com.bvas.bvas.exception;

public class InvalidOtpException extends BadRequestException {
    public InvalidOtpException(String message) {
        super(message);
    }
}
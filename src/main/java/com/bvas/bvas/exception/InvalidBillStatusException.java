package com.bvas.bvas.exception;

public class InvalidBillStatusException extends BadRequestException {
    public InvalidBillStatusException(String message) {
        super(message);
    }
}
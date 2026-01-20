package com.bvas.bvas.exception;

public class BillAlreadyExistsException extends BadRequestException {
    public BillAlreadyExistsException(String message) {
        super(message);
    }
}
package com.bvas.bvas.exception;

import com.bvas.bvas.exception.BadRequestException;



public class BillSubmissionLockedException extends BadRequestException {
    public BillSubmissionLockedException(String message) {
        super(message);
    }
}
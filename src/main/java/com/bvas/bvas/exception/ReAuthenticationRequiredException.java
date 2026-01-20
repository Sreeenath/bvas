package com.bvas.bvas.exception;

public class ReAuthenticationRequiredException extends UnauthorizedException {
    public ReAuthenticationRequiredException(String message) {
        super(message);
    }
}
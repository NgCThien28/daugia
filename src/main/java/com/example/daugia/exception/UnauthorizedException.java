package com.example.daugia.exception;
//Khong cho phep
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

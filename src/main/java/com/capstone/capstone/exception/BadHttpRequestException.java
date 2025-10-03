package com.capstone.capstone.exception;


public class BadHttpRequestException extends RuntimeException {
    public BadHttpRequestException(String message) {
        super(message);
    }
}

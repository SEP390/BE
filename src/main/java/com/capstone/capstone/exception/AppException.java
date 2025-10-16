package com.capstone.capstone.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private Object data;
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Object data) {
        super(message);
        this.data = data;
    }
}

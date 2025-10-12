package com.capstone.capstone.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final String code;
    private Object data;
    public AppException(String code) {
        this.code = code;
    }

    public AppException(String code, Object data) {
        this.code = code;
        this.data = data;
    }
}

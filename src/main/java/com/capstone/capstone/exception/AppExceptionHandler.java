package com.capstone.capstone.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(value = { AppException.class })
    public ResponseEntity<?> handleException(AppException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", e.getCode(),
                "data", e.getData()
        ));
    }
}

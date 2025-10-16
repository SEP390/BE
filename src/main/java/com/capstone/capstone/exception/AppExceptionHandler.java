package com.capstone.capstone.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(AppException.class)
    @ResponseBody
    public ResponseEntity<?> handleException(AppException e) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", e.getMessage());
        if (e.getData() != null) {
            map.put("data", e.getData());
        }
        return ResponseEntity.badRequest().body(map);
    }
}

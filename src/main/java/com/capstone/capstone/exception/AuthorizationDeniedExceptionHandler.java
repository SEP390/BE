package com.capstone.capstone.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class AuthorizationDeniedExceptionHandler {
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseBody
    public ResponseEntity<?> handleException(AuthorizationDeniedException e) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", 400);
        map.put("message", "UNAUTHORIZED");
        return ResponseEntity.badRequest().body(map);
    }
}

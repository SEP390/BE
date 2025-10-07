package com.capstone.capstone.exception;

import com.capstone.capstone.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleException(NotFoundException exception) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setMessage(exception.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadHttpRequestException.class)
    public ResponseEntity<BaseResponse<?>> handleBadException(BadHttpRequestException exception) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setMessage(exception.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage("Tên đăng nhập/email hoặc mật khẩu không đúng");
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        BaseResponse<?> response = new BaseResponse<>();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");
        response.setMessage(errorMessage);
        response.setData(null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}

package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.auth.AuthRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.auth.AuthResponse;
import com.capstone.capstone.service.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.AUTH.AUTH)
public class AuthController {

    private final IAuthService authService;

    @PostMapping()
    public ResponseEntity<BaseResponse<AuthResponse>> auth(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);
        BaseResponse<AuthResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(authResponse);
        baseResponse.setMessage("success");
        baseResponse.setStatus(HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

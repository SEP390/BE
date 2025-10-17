package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.auth.GoogleRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.GoogleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GoogleController {
    private final GoogleService googleService;

    @PostMapping("/api/google")
    public BaseResponse<?> googleAuth(@RequestBody GoogleRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", googleService.auth(request));
    }
}

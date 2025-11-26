package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.timeConfig.CreateTimeConfigRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.timeConfig.TimeConfigResponse;
import com.capstone.capstone.service.impl.TimeConfigService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TimeConfigController {
    private final TimeConfigService timeConfigService;

    @GetMapping("/api/time-config/current")
    public BaseResponse<TimeConfigResponse> getCurrent() {
        return new BaseResponse<>(timeConfigService.getCurrentResponse());
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/api/time-config")
    public BaseResponse<TimeConfigResponse> create(@RequestBody @Valid CreateTimeConfigRequest request) {
        return new BaseResponse<>(timeConfigService.create(request));
    }
}

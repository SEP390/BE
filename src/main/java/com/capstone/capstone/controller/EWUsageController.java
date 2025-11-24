package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.EWUsageService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
public class EWUsageController {
    private final EWUsageService ewUsageService;
    @GetMapping("/api/user/ew")
    public BaseResponse<?> getUserEWUsage(Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        return new BaseResponse<>(ewUsageService.getUserUsages(filter, pageable));
    }
}

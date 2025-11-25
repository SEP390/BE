package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.EWUsageService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class EWUsageController {
    private final EWUsageService ewUsageService;
    @GetMapping("/api/user/ew")
    public BaseResponse<?> getUserEWUsage(
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("semesterId", semesterId);
        filter.put("startDate", startDate);
        filter.put("endDate", endDate);
        return new BaseResponse<>(ewUsageService.getUserUsages(filter, pageable));
    }

    @GetMapping("/api/user/ew/count")
    public BaseResponse<?> getUserEWUsageCount() {
        return new BaseResponse<>(ewUsageService.getUserUsagesCount());
    }
}

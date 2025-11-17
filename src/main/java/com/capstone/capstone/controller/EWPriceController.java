package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.ew.CreateEWPriceRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.ew.EWPriceResponse;
import com.capstone.capstone.service.impl.EWPriceService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EWPriceController {
    private final EWPriceService ewPriceService;
    @GetMapping("/api/ew/price")
    public BaseResponse<EWPriceResponse> getCurrent() {
        return new BaseResponse<>(ewPriceService.getCurrentResponse());
    }
    @PostMapping("/api/ew/price")
    public BaseResponse<EWPriceResponse> create(@Valid @RequestBody CreateEWPriceRequest request) {
        return new BaseResponse<>(ewPriceService.create(request));
    }
}

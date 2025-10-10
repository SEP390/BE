package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.room.RoomPricingRequest;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.service.impl.RoomPricingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class RoomPricingController {
    private final RoomPricingService roomPricingService;

    @GetMapping("/api/pricing")
    public BaseResponse<List<RoomPricingResponse>> getAll() {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.getAll());
    }

    @PostMapping("/api/pricing")
    public BaseResponse<RoomPricingResponse> create(@Valid RoomPricingRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.create(request));
    }

    @PatchMapping("/api/pricing")
    public BaseResponse<RoomPricingResponse> update(@Valid RoomPricingRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.update(request));
    }
}

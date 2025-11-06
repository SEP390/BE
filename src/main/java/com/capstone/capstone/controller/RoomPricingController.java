package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.room.UpdateRoomPricingRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.request.room.CreateRoomPricingRequest;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.service.impl.RoomPricingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomPricingController {
    private final RoomPricingService roomPricingService;

    @GetMapping("/api/pricing")
    public BaseResponse<List<RoomPricingResponse>> getAll(
            @RequestParam(required = false) Integer totalSlot
    ) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.getAll(totalSlot));
    }

    @GetMapping("/api/pricing/{id}")
    public BaseResponse<RoomPricingResponse> get(@PathVariable UUID id) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.getById(id));
    }

    @PostMapping("/api/pricing")
    public BaseResponse<RoomPricingResponse> create(@Valid @RequestBody CreateRoomPricingRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.create(request));
    }

    @PostMapping("/api/pricing/{id}")
    public BaseResponse<RoomPricingResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomPricingRequest request) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.update(id, request));
    }
}

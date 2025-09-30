package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.service.interfaces.IRoomPricingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class RoomController {
    private final IRoomPricingService roomPricingService;

    @GetMapping("/api/rooms/pricing")
    public ResponseEntity<BaseResponse<List<RoomPricingResponse>>> getAllRoomPricing() {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.getAllRoomPricing()));
    }
}

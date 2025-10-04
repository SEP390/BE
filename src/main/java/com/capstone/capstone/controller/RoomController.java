package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.room.RoomDetailRequest;
import com.capstone.capstone.dto.request.room.RoomMatchingRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.room.RoomDetailsResponse;
import com.capstone.capstone.dto.response.room.RoomMatchingResponse;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.RoomService;
import com.capstone.capstone.service.interfaces.IRoomPricingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomController {
    private final IRoomPricingService roomPricingService;
    private final RoomService roomService;

    @GetMapping("/api/rooms/pricing")
    public ResponseEntity<BaseResponse<List<RoomPricingResponse>>> getAllRoomPricing() {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomPricingService.getAllRoomPricing()));
    }

    @GetMapping("/api/rooms/matching1y")
    public ResponseEntity<BaseResponse<List<RoomMatchingResponse>>> getRoomMatchingFirstYear(Authentication authentication) {
        UUID currentUserId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.getBookableRoomFirstYear(currentUserId)));
    }

    @PostMapping("/api/rooms/matching")
    public ResponseEntity<BaseResponse<List<RoomMatchingResponse>>> getRoomMatching(@RequestBody RoomMatchingRequest request, Authentication authentication) {
        UUID currentUserId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.getBookableRoom(currentUserId, request.getTotalSlot(), request.getDormId(), request.getFloor())));
    }

    @PostMapping("/api/rooms/details")
    public ResponseEntity<BaseResponse<RoomDetailsResponse>> getRoomDetails(@RequestBody RoomDetailRequest request) {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.getRoomDetails(request.getId())));
    }
}

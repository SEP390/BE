package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.service.impl.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/api/rooms-matching")
    public BaseResponse<List<RoomMatchingResponse>> getRoomMatching() {
        return new BaseResponse<>(roomService.getMatching());
    }

    @GetMapping("/api/rooms/{id}")
    public BaseResponse<RoomResponseJoinPricingAndDormAndSlot> getRoomById(@PathVariable UUID id) {
        return new BaseResponse<>(roomService.getResponseById(id));
    }

    @GetMapping("/api/rooms")
    public BaseResponse<PagedModel<RoomResponseJoinPricingAndDormAndSlot>> get(
            @RequestParam(required = false) UUID dormId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer totalSlot,
            @RequestParam(required = false) String roomNumber,
            @PageableDefault Pageable pageable) {
        return new BaseResponse<>(roomService.get(dormId, floor, totalSlot, roomNumber, pageable));
    }

    @GetMapping("/api/rooms/booking")
    public BaseResponse<PagedModel<RoomResponseJoinPricing>> getBooking(
            @RequestParam(required = false) UUID dormId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer totalSlot,
            @RequestParam(required = false) String roomNumber,
            @PageableDefault Pageable pageable) {
        return new BaseResponse<>(roomService.getBooking(dormId, floor, totalSlot, roomNumber, pageable));
    }

    @GetMapping("/api/rooms/current")
    public BaseResponse<RoomResponseJoinDorm> current() {
        return new BaseResponse<>(roomService.current());
    }

    @GetMapping("/api/rooms/{id}/roommates")
    public BaseResponse<List<RoommateResponse>> getRoommates(@PathVariable UUID id) {
        return new BaseResponse<>(roomService.getRoommates(id));
    }

    @GetMapping("/api/rooms/{id}/users")
    public BaseResponse<List<RoomUserResponse>> getUsers(@PathVariable UUID id) {
        return new BaseResponse<>(roomService.getUsersResponse(id));
    }

    @PostMapping("/api/rooms")
    public BaseResponse<RoomResponse> create(CreateRoomRequest request) {
        return new BaseResponse<>(roomService.create(request));
    }
}

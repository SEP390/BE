package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/api/rooms-matching")
    public BaseResponse<List<RoomMatchingResponse>> getRoomMatching(Authentication authentication) {
        User user = ((User) authentication.getPrincipal());
        return new BaseResponse<>(roomService.getRoomMatching(user));
    }

    @GetMapping("/api/rooms/{id}")
    public BaseResponse<RoomPriceDormSlotResponse> getRoomById(@PathVariable UUID id) {
        return new BaseResponse<>(roomService.getRoomById(id));
    }

    @GetMapping("/api/rooms")
    public BaseResponse<PagedModel<RoomResponse>> get(
            @RequestParam(required = false) UUID dormId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer totalSlot,
            @RequestParam(required = false) String roomNumber,
            @PageableDefault Pageable pageable) {
        return new BaseResponse<>(roomService.get(dormId, floor, totalSlot, roomNumber, pageable));
    }

    @GetMapping("/api/rooms/current")
    public BaseResponse<RoomDormResponse> current() {
        return new BaseResponse<>(roomService.current());
    }

    @GetMapping("/api/rooms/{id}/roommates")
    public BaseResponse<List<RoommateResponse>> getRoommates(@PathVariable UUID id) {
        return new BaseResponse<>(roomService.getRoommates(id));
    }

    @PostMapping("/api/rooms")
    public BaseResponse<RoomResponse> create(CreateRoomRequest request) {
        return new BaseResponse<>(roomService.create(request));
    }
}

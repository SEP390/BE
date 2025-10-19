package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.room.CurrentRoomResponse;
import com.capstone.capstone.dto.response.room.RoomDetailsResponse;
import com.capstone.capstone.dto.response.room.RoomMatchingResponse;
import com.capstone.capstone.dto.response.room.RoommateResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.RoomService;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/api/rooms-matching")
    public ResponseEntity<BaseResponse<List<RoomMatchingResponse>>> getRoomMatching(Authentication authentication) {
        User user = ((User) authentication.getPrincipal());
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.getRoomMatching(user)));
    }

    @GetMapping("/api/rooms/{id}")
    public ResponseEntity<BaseResponse<RoomDetailsResponse>> getRoomById(@PathVariable UUID id) {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.getRoomById(id)));
    }

    @GetMapping("/api/rooms")
    public BaseResponse<?> get(
            @RequestParam(required = false) UUID dormId,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer totalSlot,
            Pageable pageable) {
        return new BaseResponse<>(roomService.get(dormId, floor, totalSlot, pageable));
    }

    @GetMapping("/api/rooms/current")
    public BaseResponse<CurrentRoomResponse> current() {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.current());
    }

    @GetMapping("/api/rooms/{id}/roommates")
    public ResponseEntity<BaseResponse<List<RoommateResponse>>> getRoommates(@PathVariable UUID id) {
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", roomService.getRoommates(id)));
    }
}

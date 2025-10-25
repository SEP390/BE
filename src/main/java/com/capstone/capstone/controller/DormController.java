package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.dto.response.dorm.DormResponseJoinRoomSlot;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricing;
import com.capstone.capstone.service.impl.DormService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DormController {
    private final DormService dormService;

    @GetMapping("/api/dorms/{id}/rooms")
    public BaseResponse<List<RoomResponseJoinPricing>> getRooms(@PathVariable UUID id) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", dormService.getRooms(id));
    }

    @GetMapping("/api/dorms")
    public BaseResponse<List<DormResponse>> getAll() {
        return new BaseResponse<>(dormService.getAll());
    }

    @GetMapping("/api/dorms/{id}")
    public BaseResponse<DormResponseJoinRoomSlot> get(@PathVariable UUID id) {
        return new BaseResponse<>(dormService.getResponse(id));
    }

    @PostMapping("/api/dorms")
    public BaseResponse<?> create(CreateDormRequest request) {
        return new BaseResponse<>(dormService.create(request));
    }
}

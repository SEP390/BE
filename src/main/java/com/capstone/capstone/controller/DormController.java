package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.request.dorm.UpdateDormRequest;
import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.dto.response.dorm.DormResponseJoinRoomSlot;
import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricing;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricingAndDorm;
import com.capstone.capstone.service.impl.DormService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DormController {
    private final DormService dormService;

    /**
     * Create dorm
     * @param request request
     * @return dorm
     */
    @PostMapping("/api/dorms")
    public BaseResponse<DormResponse> create(@RequestBody @Valid CreateDormRequest request) {
        return new BaseResponse<>(dormService.create(request));
    }

    @GetMapping("/api/dorms")
    public BaseResponse<List<DormResponse>> getAll() {
        return new BaseResponse<>(dormService.getAllResponse());
    }

    @GetMapping("/api/dorms/{id}")
    public BaseResponse<DormResponseJoinRoomSlot> getById(@PathVariable UUID id) {
        return new BaseResponse<>(dormService.getResponseById(id));
    }

    @GetMapping("/api/dorms/{id}/rooms")
    public BaseResponse<List<RoomResponseJoinPricing>> getRooms(@PathVariable UUID id) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", dormService.getRooms(id));
    }

    @PostMapping("/api/dorms/{id}")
    public BaseResponse<DormResponse> update(@PathVariable UUID id, @RequestBody @Valid UpdateDormRequest request) {
        return new BaseResponse<>(dormService.update(id, request));
    }

    @PostMapping("/api/dorms/{id}/room")
    public BaseResponse<RoomResponseJoinPricingAndDorm> addRoom(@PathVariable UUID id, @RequestBody @Valid CreateRoomRequest request) {
        return new BaseResponse<>(dormService.addRoom(id, request));
    }
}

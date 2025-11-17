package com.capstone.capstone.controller;

import com.capstone.capstone.dto.request.ew.CreateEWRoomRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.service.impl.EWRoomService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class EWRoomController {
    private final EWRoomService ewRoomService;

    @GetMapping("/api/ew/room/{roomId}")
    public BaseResponse<PagedModel<EWRoomResponse>> getByRoom(@PathVariable UUID roomId, @PageableDefault Pageable pageable) {
        return new BaseResponse<>(ewRoomService.getResponseByRoom(roomId, pageable));
    }

    @PostMapping("/api/ew/room/{roomId}")
    public BaseResponse<EWRoomResponse> create(@PathVariable UUID roomId, @Valid @RequestBody CreateEWRoomRequest request) {
        return new BaseResponse<>(ewRoomService.create(roomId, request));
    }
}

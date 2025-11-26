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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class EWRoomController {
    private final EWRoomService ewRoomService;

    @GetMapping("/api/ew/room")
    public BaseResponse<PagedModel<EWRoomResponse>> getAll(
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault Pageable pageable) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("roomId", roomId);
        filter.put("semesterId", semesterId);
        filter.put("startDate", startDate);
        filter.put("endDate", endDate);
        return new BaseResponse<>(ewRoomService.getAll(filter, pageable));
    }

    @PostMapping("/api/ew/room")
    public BaseResponse<EWRoomResponse> create(@Valid @RequestBody CreateEWRoomRequest request) {
        return new BaseResponse<>(ewRoomService.create(request));
    }
}

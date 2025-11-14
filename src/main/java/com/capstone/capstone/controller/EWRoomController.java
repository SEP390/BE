package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.service.impl.EWRoomService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EWRoomController {
    private final EWRoomService ewRoomService;

    @GetMapping("/api/ewroom/latest")
    public BaseResponse<EWRoomResponse> getLatest() {
        return new BaseResponse<>(ewRoomService.getLatestResponse());
    }
}

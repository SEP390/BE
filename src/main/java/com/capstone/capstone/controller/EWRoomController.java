package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.ew.EWRoomResponse;
import com.capstone.capstone.entity.BaseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EWRoomController {
    @GetMapping("/api/ewroom/latest")
    public BaseEntity<EWRoomResponse> getLatest() {
    }
}

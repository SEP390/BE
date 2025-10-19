package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.request.dorm.BookableDormRequest;
import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
import com.capstone.capstone.dto.response.room.RoomResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.DormService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class DormController {
    private final DormService dormService;

    @PostMapping("/api/dorms/bookable")
    public ResponseEntity<BaseResponse<List<BookableDormResponse>>> getBookableDorm(@RequestBody BookableDormRequest request, Authentication authentication) {
        GenderEnum gender = ((User) authentication.getPrincipal()).getGender();
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", dormService.getBookableDorm(request.getTotalSlot(), gender)));
    }

    @GetMapping("/api/dorms/{id}/rooms")
    public BaseResponse<List<RoomResponse>> getRooms(@PathVariable UUID id) {
        return new BaseResponse<>(HttpStatus.OK.value(), "success", dormService.getRooms(id));
    }

    @GetMapping("/api/dorms")
    public BaseResponse<?> getList() {
        return new BaseResponse<>(dormService.getList());
    }

    @GetMapping("/api/dorms/{id}")
    public BaseResponse<?> get(@PathVariable UUID id) {
        return new BaseResponse<>(dormService.getResponse(id));
    }

    @PostMapping("/api/dorms")
    public BaseResponse<?> create(CreateDormRequest request) {
        return new BaseResponse<>(dormService.create(request));
    }
}

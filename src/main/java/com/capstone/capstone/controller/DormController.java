package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.DormService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class DormController {
    private final DormService dormService;

    @PostMapping("/api/dorms/bookable")
    public ResponseEntity<BaseResponse<List<BookableDormResponse>>> getBookableDorm(int totalSlot, UserDetails userDetails) {
        GenderEnum gender = ((User) userDetails).getGender();
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", dormService.getBookableDorm(totalSlot, gender)));
    }
}

package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.request.dorm.BookableDormRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.interfaces.IDormService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class DormController {
    private final IDormService dormService;

    @PostMapping("/api/dorms/bookable")
    public ResponseEntity<BaseResponse<List<BookableDormResponse>>> getBookableDorm(@RequestBody BookableDormRequest request, Authentication authentication) {
        GenderEnum gender = ((User) authentication.getPrincipal()).getGender();
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "success", dormService.getBookableDorm(request.getTotalSlot(), gender)));
    }
}

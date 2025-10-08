package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.user.RegisterUserRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.user.ProfileUserResponse;
import com.capstone.capstone.dto.response.user.RegisterUserResponse;
import com.capstone.capstone.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.USER.USER)
public class UserController {
    private final IUserService userService;

    @PostMapping(ApiConstant.USER.REGISTER)
    public ResponseEntity<BaseResponse<RegisterUserResponse>> RegisterUser(@RequestBody RegisterUserRequest registerUserRequest) {
        RegisterUserResponse registerUserResponse = userService.register(registerUserRequest);
        BaseResponse<RegisterUserResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(registerUserResponse);
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Register Successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileUserResponse> getUserById(@PathVariable UUID id) {
        ProfileUserResponse profileUserResponse = userService.getById(id);
        return ResponseEntity.ok(profileUserResponse);
    }

    @GetMapping("/listuser")
    public ResponseEntity<List<ProfileUserResponse>> getAllUser(){
        return ResponseEntity.ok(userService.getALl());

    }
}

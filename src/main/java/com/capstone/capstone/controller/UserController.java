package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.user.CreateUserRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.user.CreateAccountResponse;
import com.capstone.capstone.dto.response.user.GetAllResidentResponse;
import com.capstone.capstone.dto.response.user.GetUserByIdResponse;
import com.capstone.capstone.dto.response.user.GetUserInformationResponse;
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

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<CreateAccountResponse>> RegisterUser(@RequestBody CreateUserRequest createUserRequest) {
        CreateAccountResponse createAccountResponse = userService.createAccount(createUserRequest);
        BaseResponse<CreateAccountResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(createAccountResponse);
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Register Successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }
    @GetMapping(ApiConstant.USER.PROFILE)
    public ResponseEntity<BaseResponse<GetUserInformationResponse>> getUserCurrentInformation() {
        GetUserInformationResponse getUserInformationResponse = userService.getCurrentUserInformation();
        BaseResponse<GetUserInformationResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(getUserInformationResponse);
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get User Information Successfully");
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @GetMapping(ApiConstant.USER.GET_ALL_RESIDENT)
    public ResponseEntity<BaseResponse<List<GetAllResidentResponse>>> getAllResidents() {
        List<GetAllResidentResponse> residents = userService.getAllResidents();
        BaseResponse<List<GetAllResidentResponse>> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get All Residents Successfully");
        baseResponse.setData(residents);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }


    @GetMapping(ApiConstant.USER.GET_RESIDENT_BY_ID)
    public ResponseEntity<BaseResponse<GetUserByIdResponse>> getUserById(@PathVariable UUID id) {
        GetUserByIdResponse getUserByIdResponse = userService.getUserById(id);
        BaseResponse<GetUserByIdResponse> baseResponse = new BaseResponse<>();
        baseResponse.setData(getUserByIdResponse);
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get User ById Successfully");
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

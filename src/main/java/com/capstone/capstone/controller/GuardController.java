package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.guard.CreateGuardRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.guard.CreateGuardResponse;
import com.capstone.capstone.service.interfaces.IGuardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.GUARD.GUARD)
public class GuardController {

    private final IGuardService iGuardService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateGuardResponse>> createGuard(@RequestBody CreateGuardRequest createGuardRequest) {
        CreateGuardResponse createGuardResponse = iGuardService.createGuard(createGuardRequest);
        BaseResponse<CreateGuardResponse> response = new BaseResponse<>();
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Create Guard Successfully");
        response.setData(createGuardResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

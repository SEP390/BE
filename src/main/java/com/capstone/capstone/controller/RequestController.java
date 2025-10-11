package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.service.interfaces.IRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.REQUEST.REQUEST)
public class RequestController {
    private final IRequestService requestService;

    @PostMapping(ApiConstant.REQUEST.CREATE)
    public ResponseEntity<BaseResponse<CreateRequestResponse>> createRequest(@RequestBody CreateRequestRequest createRequestRequest){
        CreateRequestResponse createRequestResponse = requestService.createRequest(createRequestRequest);
        BaseResponse<CreateRequestResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Request created");
        baseResponse.setData(createRequestResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }
}

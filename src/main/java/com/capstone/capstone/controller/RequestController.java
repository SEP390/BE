package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllRequestResponse;
import com.capstone.capstone.dto.response.request.GetRequestByIdResponse;
import com.capstone.capstone.dto.response.request.UpdateRequestResponse;
import com.capstone.capstone.service.interfaces.IRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.REQUEST.REQUEST)
public class RequestController {
    private final IRequestService requestService;

    @PostMapping()
    public ResponseEntity<BaseResponse<CreateRequestResponse>> createRequest(@RequestBody CreateRequestRequest createRequestRequest){
        CreateRequestResponse createRequestResponse = requestService.createRequest(createRequestRequest);
        BaseResponse<CreateRequestResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Request created");
        baseResponse.setData(createRequestResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }

    @PutMapping(ApiConstant.REQUEST.UPDATE)
    public ResponseEntity<BaseResponse<UpdateRequestResponse>> updateRequest(@RequestBody UpdateRequestRequest updateRequestRequest,@PathVariable UUID id){
        UpdateRequestResponse updateRequestResponse = requestService.updateRequest(updateRequestRequest, id);
        BaseResponse<UpdateRequestResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Request updated");
        baseResponse.setData(updateRequestResponse);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }


    @GetMapping(ApiConstant.REQUEST.GET_BY_ID)
    public ResponseEntity<BaseResponse<GetRequestByIdResponse>> getRequestById(@PathVariable UUID id){
        GetRequestByIdResponse getRequestByIdResponse = requestService.getRequestById(id);
        BaseResponse<GetRequestByIdResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get request by ID successfully");
        baseResponse.setData(getRequestByIdResponse);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @GetMapping()
    public ResponseEntity<BaseResponse<List<GetAllRequestResponse>>> getAllRequests(){
        List<GetAllRequestResponse> response = requestService.getAllRequest();
        BaseResponse<List<GetAllRequestResponse>> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get requests successfully");
        baseResponse.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

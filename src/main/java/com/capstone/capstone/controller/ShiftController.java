package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.Shift.CreateShiftRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.Shift.CreateShiftResponse;
import com.capstone.capstone.service.impl.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.SHIFT.SHIFT)
public class ShiftController {
    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateShiftResponse>> createShift(@RequestBody CreateShiftRequest createShiftRequest) {
        CreateShiftResponse createShiftResponse = shiftService.createShift(createShiftRequest);
        BaseResponse<CreateShiftResponse> response = new BaseResponse<>(createShiftResponse);
        response.setData(createShiftResponse);
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Successfully created shift");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

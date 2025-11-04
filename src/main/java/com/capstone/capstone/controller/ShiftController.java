package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.shift.CreateShiftRequest;
import com.capstone.capstone.dto.request.shift.UpdateShiftRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.shift.CreateShiftResponse;
import com.capstone.capstone.dto.response.shift.GetAllShiftResponse;
import com.capstone.capstone.dto.response.shift.UpdateShiftResponse;
import com.capstone.capstone.service.impl.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetAllShiftResponse>>> getAllShifts() {
        BaseResponse<List<GetAllShiftResponse>> response = new BaseResponse<>();
        List<GetAllShiftResponse> getAllShiftResponseList = shiftService.getAllShifts();
        response.setData(getAllShiftResponseList);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Successfully retrieved all shifts");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(ApiConstant.SHIFT.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateShiftResponse>> updateShift(@PathVariable UUID id, @RequestBody UpdateShiftRequest updateShiftRequest) {
        BaseResponse<UpdateShiftResponse> response = new BaseResponse<>();
        UpdateShiftResponse updateShiftResponse = shiftService.updateShift(id, updateShiftRequest);
        response.setData(updateShiftResponse);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Successfully updated shift");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

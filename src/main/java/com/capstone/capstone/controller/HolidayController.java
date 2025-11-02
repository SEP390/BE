package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.holiday.CreateHolidayRequest;
import com.capstone.capstone.dto.request.holiday.UpdateHolidayRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.holiday.CreateHolidayResponse;
import com.capstone.capstone.dto.response.holiday.GetAllHolidayResponse;
import com.capstone.capstone.dto.response.holiday.UpdateHolidayResponse;
import com.capstone.capstone.dto.response.request.UpdateRequestResponse;
import com.capstone.capstone.entity.BaseEntity;
import com.capstone.capstone.service.impl.HolidayService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping(ApiConstant.HOLIDAY.HOLIDAY)
public class HolidayController {
    private final HolidayService holidayService;

    @PostMapping()
    ResponseEntity<BaseResponse<CreateHolidayResponse>> createHoliday(@RequestBody CreateHolidayRequest createHolidayRequest){
        CreateHolidayResponse createHolidayResponse = holidayService.createHoliday(createHolidayRequest);
        BaseResponse<CreateHolidayResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Created holiday");
        baseResponse.setData(createHolidayResponse);
        return new ResponseEntity<>(baseResponse, HttpStatus.CREATED);
    }

    @PutMapping(ApiConstant.HOLIDAY.GET_BY_ID)
    ResponseEntity<BaseResponse<UpdateHolidayResponse>> updateHoliday(@PathVariable UUID id, @RequestBody UpdateHolidayRequest updateRequestRequest){
        UpdateHolidayResponse updateHolidayResponse = holidayService.updateHoliday(id, updateRequestRequest);
        BaseResponse<UpdateHolidayResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Updated holiday");
        baseResponse.setData(updateHolidayResponse);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping()
    ResponseEntity<BaseResponse<List<GetAllHolidayResponse>>>  getAllHolidays(){
        List<GetAllHolidayResponse> holidays = holidayService.getAllHoliday();
        BaseResponse<List<GetAllHolidayResponse>> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("All holidays");
        baseResponse.setData(holidays);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }
}

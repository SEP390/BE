package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.request.schedule.UpdateScheduleRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.PageResponse;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;
import com.capstone.capstone.dto.response.schedule.GetScheduleResponse;
import com.capstone.capstone.dto.response.schedule.UpdateScheduleResponse;
import com.capstone.capstone.entity.BaseEntity;
import com.capstone.capstone.service.impl.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.PutExchange;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.SCHEDULE.SCHEDULE)
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<BaseResponse<List<CreateScheduleResponse>>> createSchedule(@RequestBody CreateScheduleRequest createScheduleRequest) {
        BaseResponse<List<CreateScheduleResponse>> response = new BaseResponse<>();
        List<CreateScheduleResponse> schedules = scheduleService.createSchedule(createScheduleRequest);
        response.setData(schedules);
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Schedule created");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @GetMapping
//    public ResponseEntity<BaseResponse<PageResponse<GetScheduleResponse>>> GetAllSchedules(@RequestParam int page, @RequestParam int size) {
//        Pageable pageable = Pageable.ofSize(size).withPage(page - 1);
//        BaseResponse<PageResponse<GetScheduleResponse>> response = new BaseResponse<>();
//        PageResponse<GetScheduleResponse> schedules = scheduleService.getAllSchedules(pageable);
//        response.setData(schedules);
//        response.setStatus(HttpStatus.OK.value());
//        response.setMessage("Schedules found");
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
    
    @PutMapping(ApiConstant.SCHEDULE.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateScheduleResponse>> updateSchedule(@RequestBody UpdateScheduleRequest updateScheduleRequest, @PathVariable UUID id) {
        BaseResponse<UpdateScheduleResponse> response = new BaseResponse<>();
        UpdateScheduleResponse r = scheduleService.updateSchedule(updateScheduleRequest, id);
        response.setData(r);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Schedule updated");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetScheduleResponse>>> getAllScheduleByDate(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        BaseResponse<List<GetScheduleResponse>> response = new BaseResponse<>();
        List<GetScheduleResponse> schedules = scheduleService.getAllScheduleByDate(from, to);
        response.setData(schedules);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Schedules found");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

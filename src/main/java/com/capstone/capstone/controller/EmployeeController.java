package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.service.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.EMPLOYEE.EMPLOYEE)
public class EmployeeController {

    private final IEmployeeService iEmployeeService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateEmployeeResponse>> createEmployee(@RequestBody CreateEmployeeRequest createEmployeeRequest) {
        CreateEmployeeResponse createEmployeeResponse = iEmployeeService.createEmployee(createEmployeeRequest);
        BaseResponse<CreateEmployeeResponse> response = new BaseResponse<>();
        response.setStatus(HttpStatus.CREATED.value());
        response.setMessage("Create Employee Successfully");
        response.setData(createEmployeeResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

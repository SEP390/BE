package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetAllEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetEmployeeById;
import com.capstone.capstone.dto.response.employee.UpdateEmployeeResponse;
import com.capstone.capstone.service.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetAllEmployeeResponse>>>  getAllEmployees() {
        List<GetAllEmployeeResponse> employees = iEmployeeService.getAllEmployee();
        BaseResponse<List<GetAllEmployeeResponse>> response = new BaseResponse<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Get All Employee Successfully");
        response.setData(employees);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(ApiConstant.EMPLOYEE.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateEmployeeResponse>> updateEmployee(@PathVariable UUID id, @RequestBody UpdateEmployeeRequest request) {
        UpdateEmployeeResponse updateEmployeeResponse = iEmployeeService.updateEmployee(id, request);
        BaseResponse<UpdateEmployeeResponse> response = new BaseResponse<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Update Employee Successfully");
        response.setData(updateEmployeeResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(ApiConstant.EMPLOYEE.GET_BY_ID)
    public ResponseEntity<BaseResponse<GetEmployeeById>> getEmployeeById(@PathVariable UUID id) {
        GetEmployeeById getEmployeeById = iEmployeeService.getEmployeeById(id);
        BaseResponse<GetEmployeeById> response = new BaseResponse<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Get Employee ById Successfully");
        response.setData(getEmployeeById);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

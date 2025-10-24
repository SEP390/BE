package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetAllEmployeeResponse;

import java.util.List;

public interface IEmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
    List<GetAllEmployeeResponse> getAllEmployee();
}

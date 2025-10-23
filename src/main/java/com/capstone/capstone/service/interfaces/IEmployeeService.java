package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;

public interface IEmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
}

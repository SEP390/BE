package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetAllEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetEmployeeById;
import com.capstone.capstone.dto.response.employee.UpdateEmployeeResponse;

import java.util.List;
import java.util.UUID;

public interface IEmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
    List<GetAllEmployeeResponse> getAllEmployee();
    GetEmployeeById getEmployeeById(UUID employeeId);
    UpdateEmployeeResponse updateEmployee(UUID employeeId,UpdateEmployeeRequest request);
}

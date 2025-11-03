package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.ResetPasswordRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.*;

import java.util.List;
import java.util.UUID;

public interface IEmployeeService {
    CreateEmployeeResponse createEmployee(CreateEmployeeRequest request);
    List<GetAllEmployeeResponse> getAllEmployee();
    GetEmployeeByIdResponse getEmployeeById(UUID employeeId);
    UpdateEmployeeResponse updateEmployee(UUID employeeId,UpdateEmployeeRequest request);
    ResetPasswordResponse resetPassword(UUID employeeId, ResetPasswordRequest request);
}

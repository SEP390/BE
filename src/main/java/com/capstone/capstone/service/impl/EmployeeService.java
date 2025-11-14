package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.ResetPasswordRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.*;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Schedule;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.ScheduleRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        List<User> users = userRepository.findAll();
        if (users.stream().anyMatch(user -> user.getUsername().equals(request.getUsername()))) {
            throw new BadHttpRequestException("Username is already taken");
        }
        if (users.stream().anyMatch(user -> user.getEmail().equals(request.getEmail()))) {
            throw new BadHttpRequestException("Email is already taken");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setUserCode(request.getUserCode());
        user.setGender(request.getGender());
        user.setRole(request.getRole());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFullName(request.getFullName());
        userRepository.save(user);
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setHireDate(request.getHireDate());
        employee.setContractEndDate(request.getContractEndDate());
        employeeRepository.save(employee);
        CreateEmployeeResponse response = new CreateEmployeeResponse();
        response.setEmail(request.getEmail());
        response.setUsername(request.getUsername());
        response.setRole(request.getRole());
        response.setEmployeeId(employee.getId());
        return  response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetAllEmployeeResponse> getAllEmployee() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toGetAllEmployeeResponse)
                .toList();
    }


    private GetAllEmployeeResponse toGetAllEmployeeResponse(Employee e) {
        GetAllEmployeeResponse dto = new GetAllEmployeeResponse();
        dto.setEmployeeId(e.getId());

        User u = e.getUser();           // có thể null tùy dữ liệu
        dto.setUsername(u != null ? u.getUsername() : null);
        dto.setRole(u != null ? u.getRole() : null);   // RoleEnum
        dto.setPhone(u != null ? u.getPhoneNumber() : null);
        dto.setEmail(u != null ? u.getEmail() : null);
        return dto;
    }

    @Override
    public GetEmployeeByIdResponse getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        User user = employee.getUser();
        GetEmployeeByIdResponse response = new GetEmployeeByIdResponse();
        response.setEmployeeId(employeeId);
        response.setUserId(user.getId());
        response.setUserCode(user.getUserCode());
        response.setDob(user.getDob());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setGender(user.getGender());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        return response;
    }

    @Override
    public UpdateEmployeeResponse updateEmployee(UUID employeeId ,UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        User user = userRepository.findById(employee.getUser().getId()).orElseThrow(() -> new NotFoundException("User not found"));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDob(request.getBirthDate());
        user.setRole(request.getRole());
        employee.setContractEndDate(request.getContractEndDate());
        userRepository.save(user);
        return null;
    }

    @Override
    public ResetPasswordResponse resetPassword(UUID employeeId, ResetPasswordRequest request) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        User user = employee.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setEmployeeId(employee.getId());
        return  response;
    }
}

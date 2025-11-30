package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.ResetPasswordRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.*;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IEmployeeService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        UUID uuid = AuthenUtil.getCurrentUserId();
        User curentUser = userRepository.findById(uuid).orElseThrow(()-> new NotFoundException("User not found"));

        if (curentUser.getRole() != RoleEnum.MANAGER) {
            throw new BadHttpRequestException("You do not have permission to create employees");
        }
        List<User> users = userRepository.findAll();
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadHttpRequestException("Email is required");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadHttpRequestException("Username is required");
        }
        if (users.stream().anyMatch(u -> request.getUsername().equals(u.getUsername()))) {
            throw new BadHttpRequestException("Username is already taken");
        }
        if (users.stream().anyMatch(u -> request.getEmail().equals(u.getEmail()))) {
            throw new BadHttpRequestException("Email is already taken");
        }
        if (request.getRole() != RoleEnum.CLEANER && request.getRole() != RoleEnum.GUARD && request.getRole() != RoleEnum.TECHNICAL) {
            throw new BadHttpRequestException("You only can create account for GUARD, CLEANER or TECHNICAL");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BadHttpRequestException("Invalid email format");
        }
        if (request.getRole() != RoleEnum.GUARD && request.getRole() != RoleEnum.CLEANER) {
            throw new BadHttpRequestException("Employee role must be GUARD or CLEANER");
        }
        if (request.getHireDate() != null && request.getContractEndDate() != null &&
                request.getContractEndDate().isBefore(request.getHireDate())) {
            throw new BadHttpRequestException("Contract end date must be after hire date");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadHttpRequestException("Password is required");
        }
        if (request.getPassword().length() < 6) {
            throw new BadHttpRequestException("Password must be at least 6 characters");
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
        user.setImage(request.getImage());
        userRepository.save(user);
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setHireDate(request.getHireDate());
        employee.setContractEndDate(request.getContractEndDate());
        employee = employeeRepository.save(employee);
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
        UUID uuid = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(uuid).orElseThrow(()-> new NotFoundException("User not found"));
        if (user.getRole() != RoleEnum.MANAGER) {
            throw new BadHttpRequestException("You do not have permission to get employees");
        }
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
        response.setHireDate(employee.getHireDate());
        response.setContractEndDate(employee.getContractEndDate());
        response.setImageUrl(user.getImage());
        return response;
    }

    @Override
    public UpdateEmployeeResponse updateEmployee(UUID employeeId ,UpdateEmployeeRequest request) {
        UUID currentUserId = AuthenUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found"));
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));

        boolean isManager = currentUser.getRole() == RoleEnum.MANAGER;
        boolean isOwner = currentUser.getId().equals(employee.getUser().getId());

        if (!isManager && !isOwner) {
            throw new BadHttpRequestException("You are not allowed to update this employee");
        }
        if (request.getRole() != RoleEnum.CLEANER && request.getRole() != RoleEnum.GUARD && request.getRole() != RoleEnum.TECHNICAL) {
            throw new BadHttpRequestException("You only can update this employee to GUARD, CLEANER or TECHNICAL");
        }

        if (employee.getHireDate() != null && request.getContractEndDate() != null &&
                request.getContractEndDate().isBefore(employee.getHireDate())) {
            throw new BadHttpRequestException("Contract end date must be after hire date");
        }
        User user = employee.getUser();
        user.setPhoneNumber(request.getPhoneNumber());
        user.setDob(request.getBirthDate());
        user.setRole(request.getRole());
        employee.setContractEndDate(request.getContractEndDate());
        userRepository.save(user);
        employeeRepository.save(employee);

        UpdateEmployeeResponse resp = new UpdateEmployeeResponse();
        resp.setEmployeeId(employee.getId());
        resp.setFullName(user.getFullName());
        resp.setBirthDate(user.getDob());
        resp.setContractEndDate(request.getContractEndDate());
        return resp;
    }

    @Override
    public ResetPasswordResponse resetPassword(UUID employeeId, ResetPasswordRequest request) {
        UUID currentUserId = AuthenUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new NotFoundException("User not found"));
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        User user = employee.getUser();
        boolean isManager = currentUser.getRole() == RoleEnum.MANAGER;
        boolean isOwner = currentUser.getId().equals(employee.getUser().getId());

        if (!isManager && !isOwner) {
            throw new BadHttpRequestException("You are not allowed to update this employee");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BadHttpRequestException("Password is required");
        }
        if (request.getNewPassword().length() < 6) {
            throw new BadHttpRequestException("Password must be at least 6 characters");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        ResetPasswordResponse response = new ResetPasswordResponse();
        response.setEmployeeId(employee.getId());
        return  response;
    }
}

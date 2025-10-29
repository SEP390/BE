package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetAllEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetEmployeeById;
import com.capstone.capstone.dto.response.employee.UpdateEmployeeResponse;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DormRepository dormRepository;
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
        employee.setDorm(dormRepository.findById(request.getDormId()).orElseThrow(() -> new NotFoundException("Dorm not found")));
        employeeRepository.save(employee);
        CreateEmployeeResponse response = new CreateEmployeeResponse();
        response.setEmail(request.getEmail());
        response.setUsername(request.getUsername());
        response.setRole(request.getRole());
        return  response;
    }

    @Override
    public List<GetAllEmployeeResponse> getAllEmployee() {
        List<Employee> employees = employeeRepository.findAll();
        List<GetAllEmployeeResponse> response = new ArrayList<>();
        for (Employee employee : employees) {
            GetAllEmployeeResponse response1 = new GetAllEmployeeResponse();
            response1.setEmployeeId(employee.getId());
            response1.setEmail(employee.getUser().getEmail());
            response1.setUsername(employee.getUser().getUsername());
            response1.setRole(employee.getUser().getRole());
            response1.setEmployeeId(employee.getUser().getId());
            response1.setDormName(employee.getDorm().getDormName());
            response1.setPhone(employee.getUser().getPhoneNumber());
            response.add(response1);
        }
        return response;
    }

    @Override
    public GetEmployeeById getEmployeeById(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NotFoundException("Employee not found"));
        User user = employee.getUser();
        GetEmployeeById response = new GetEmployeeById();
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
        employee.setDorm(dormRepository.findById(request.getDormId()).orElseThrow(() -> new NotFoundException("Dorm not found")));
        userRepository.save(user);
        return null;
    }
}

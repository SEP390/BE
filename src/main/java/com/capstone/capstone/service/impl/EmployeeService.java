package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DormRepository dormRepository;

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
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setRole(RoleEnum.GUARD);
        userRepository.save(user);
        Employee employee = new Employee();
        employee.setUser(user);
        employeeRepository.save(employee);
        CreateEmployeeResponse response = new CreateEmployeeResponse();
        response.setEmail(request.getEmail());
        response.setUsername(request.getUsername());
        return  response;
    }
}

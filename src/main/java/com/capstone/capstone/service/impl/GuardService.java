package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Schedule;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.ScheduleRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GuardService {
    private final EmployeeRepository employeeRepository;
    private final ScheduleRepository scheduleRepository;

    public Dorm getGuardDorm() {
        User user = SecurityUtils.getCurrentUser();
        Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new AppException("EMPLOYEE_NOT_FOUND"));
        var schedules = scheduleRepository.findByEmployee(employee, PageRequest.of(0,1)).getContent();
        if (schedules.isEmpty()) throw new AppException("SCHEDULE_NOT_FOUND");
        return schedules.getFirst().getDorm();
    }
}

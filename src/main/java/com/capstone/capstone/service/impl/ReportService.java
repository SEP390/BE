package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.response.report.CreateReportResponse;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Report;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.ReportRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IReportService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public CreateReportResponse createReport(CreateReportRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Employee not found"));
        Report report = new Report();
        report.setContent(request.getContent());
        report.setReportStatus(ReportStatusEnum.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setUserCode(user.getUserCode());
        report.setEmployee(employee);
        reportRepository.save(report);
        CreateReportResponse createReportResponse = new CreateReportResponse();
        createReportResponse.setContent(report.getContent());
        createReportResponse.setReportStatus(report.getReportStatus());
        createReportResponse.setCreatedAt(report.getCreatedAt());
        createReportResponse.setUserCode(report.getUserCode());
        return createReportResponse;
    }
}

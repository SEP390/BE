package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.enums.ReportTypeEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.request.report.UpdateReportRequest;
import com.capstone.capstone.dto.response.report.CreateReportResponse;
import com.capstone.capstone.dto.response.report.GetAllReportResponse;
import com.capstone.capstone.dto.response.report.UpdateReportResponse;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        report.setReportType(request.getReportType());
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
        createReportResponse.setReportType(report.getReportType());
        return createReportResponse;
    }

    @Override
    public List<GetAllReportResponse> getAllReports() {
        UUID  userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        List<Report> reports;
        if (user.getRole() == RoleEnum.MANAGER) {
            reports = reportRepository.findAll();
        } else if (user.getRole() == RoleEnum.TECHNICAL) {
            reports = reportRepository.findByReportType(ReportTypeEnum.MAINTENANCE_REQUEST);
        } else if  (user.getRole() == RoleEnum.GUARD ||  user.getRole() == RoleEnum.CLEANER) {
            Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Employee not found"));
            reports = reportRepository.findByEmployeeId(employee.getId());
        } else {
            throw new AccessDeniedException("Forbidden");
        }
        List<GetAllReportResponse> responses = new ArrayList<>();
        for (Report report : reports) {
            GetAllReportResponse response = new GetAllReportResponse();
            response.setReportId(report.getId());
            response.setEmployeeId(report.getEmployee().getId());
            response.setContent(report.getContent());
            response.setResponseMessage(report.getResponseMessage());
            response.setReportStatus(report.getReportStatus());
            response.setCreatedDate(report.getCreatedAt());
            response.setUserCode(report.getUserCode());
            response.setEmployeeName(report.getEmployee().getUser().getFullName());
            response.setReportType(report.getReportType());
            responses.add(response);
        }
        return responses;
    }

    @Override
    public UpdateReportResponse updateReport(UUID requestId, UpdateReportRequest request) {
        Report report =  reportRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Report not found"));
        report.setResponseMessage(request.getResponseMessage());
        report.setReportStatus(request.getReportStatus());
        reportRepository.save(report);
        UpdateReportResponse response = new UpdateReportResponse();
        response.setReportId(report.getId());
        response.setEmployeeId(report.getEmployee().getId());
        response.setContent(report.getContent());
        response.setReportStatus(report.getReportStatus());
        response.setCreatedDate(report.getCreatedAt());
        response.setUserCode(report.getUserCode());
        response.setResponseMessage(report.getResponseMessage());
        return response;
    }


}

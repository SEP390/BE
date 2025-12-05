package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.ReportStatusEnum;
import com.capstone.capstone.dto.enums.ReportTypeEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.report.CreateReportRequest;
import com.capstone.capstone.dto.request.report.UpdateReportRequest;
import com.capstone.capstone.dto.response.report.CreateReportResponse;
import com.capstone.capstone.dto.response.report.GetAllReportResponse;
import com.capstone.capstone.dto.response.report.GetReportByIdResponse;
import com.capstone.capstone.dto.response.report.UpdateReportResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.service.interfaces.IReportService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;
    private final SlotRepository slotRepository;
    private final SemesterRepository semesterRepository;

    @Override
    public CreateReportResponse createReport(CreateReportRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Employee not found"));
        Semester semester = Optional.ofNullable(semesterRepository.findCurrent())
                .orElseThrow(() -> new RuntimeException("No active semester"));
        Report report = new Report();
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadHttpRequestException("Content cannot be blank");
        }

        if (request.getReportType() == null) {
            throw new BadHttpRequestException("Report type is required");
        }

        if (request.getReportType().equals(ReportTypeEnum.SECURITY_ISSUE)) {
            if (request.getRoomId() == null || request.getRoomId().toString().isEmpty()) {
                User resident = userRepository.findByIdAndRole(request.getResidentId(), RoleEnum.RESIDENT).orElseThrow(() -> new RuntimeException("Resident not found"));
                Room room = slotRepository.findByUser(resident).getRoom();
                report.setRoom(room);
                report.setResident(resident);
                report.setContent(request.getContent());
                report.setReportType(request.getReportType());
                report.setReportStatus(ReportStatusEnum.PENDING);
                report.setCreatedAt(LocalDateTime.now());
                report.setUserCode(user.getUserCode());
                report.setEmployee(employee);
                report.setSemester(semester);
                reportRepository.save(report);
            } else if (request.getResidentId() == null || request.getResidentId().toString().isEmpty()) {
                Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new RuntimeException("Room not found"));
                List<User> listResidentInRoom = roomRepository.findUsers(room);
                for (User resident : listResidentInRoom) {
                    report.setRoom(room);
                    report.setResident(resident);
                    report.setContent(request.getContent());
                    report.setReportType(request.getReportType());
                    report.setReportStatus(ReportStatusEnum.PENDING);
                    report.setCreatedAt(LocalDateTime.now());
                    report.setUserCode(user.getUserCode());
                    report.setEmployee(employee);
                    report.setSemester(semester);
                    reportRepository.save(report);
                }
            } else {
                Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new RuntimeException("Room not found"));
                User resident = userRepository.findByIdAndRole(request.getResidentId(), RoleEnum.RESIDENT).orElseThrow(() -> new RuntimeException("Resident not found"));
                report.setRoom(room);
                report.setResident(resident);
                report.setContent(request.getContent());
                report.setReportType(request.getReportType());
                report.setReportStatus(ReportStatusEnum.PENDING);
                report.setCreatedAt(LocalDateTime.now());
                report.setUserCode(user.getUserCode());
                report.setEmployee(employee);
                report.setSemester(semester);
                reportRepository.save(report);
            }
        } else {
            report.setResident(null);
            report.setRoom(null);
            report.setContent(request.getContent());
            report.setReportType(request.getReportType());
            report.setReportStatus(ReportStatusEnum.PENDING);
            report.setCreatedAt(LocalDateTime.now());
            report.setUserCode(user.getUserCode());
            report.setEmployee(employee);
            report.setSemester(semester);
            reportRepository.save(report);
        }

        CreateReportResponse createReportResponse = new CreateReportResponse();
        createReportResponse.setReportId(report.getId());
        createReportResponse.setSemesterId(semester.getId());
        createReportResponse.setContent(report.getContent());
        createReportResponse.setReportStatus(report.getReportStatus());
        createReportResponse.setCreatedAt(report.getCreatedAt());
        createReportResponse.setUserCode(report.getUserCode());
        createReportResponse.setReportType(report.getReportType());
        return createReportResponse;
    }

    @Override
    public List<GetAllReportResponse> getAllReports() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        List<Report> reports;
        if (user.getRole() == RoleEnum.MANAGER || user.getRole() == RoleEnum.ADMIN) {
            reports = reportRepository.findAll();
        } else if (user.getRole() == RoleEnum.TECHNICAL) {
            reports = reportRepository.findByReportType(ReportTypeEnum.MAINTENANCE_REQUEST);
        } else if (user.getRole() == RoleEnum.GUARD || user.getRole() == RoleEnum.CLEANER) {
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
            response.setRoomId(report.getRoom().getId());
            response.setResidentId(report.getResident().getId());
            response.setSemesterId(report.getSemester().getId());
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
    public UpdateReportResponse updateReport(UUID reportId, UpdateReportRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new NotFoundException("Report not found"));
//        Employee employee = employeeRepository.findByUser(user).orElseThrow(() -> new NotFoundException("Employee not found"));
        if (user.getRole() == RoleEnum.MANAGER || user.getRole() == RoleEnum.TECHNICAL || user.getRole() == RoleEnum.CLEANER || user.getRole() == RoleEnum.GUARD) {
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
        } else {
            throw new AccessDeniedException("Access denied");
        }

    }

    @Override
    public GetReportByIdResponse getReportById(UUID reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new NotFoundException("Report not found"));
        GetReportByIdResponse response = new GetReportByIdResponse();
        response.setReportId(report.getId());
        response.setRoomId(report.getRoom().getId());
        response.setResidentId(report.getResident().getId());
        response.setEmployeeId(report.getEmployee().getId());
        response.setEmployeeName(report.getEmployee().getUser().getFullName());
        response.setContent(report.getContent());
        response.setResponseMessage(report.getResponseMessage());
        response.setCreatedDate(report.getCreatedAt());
        response.setReportStatus(report.getReportStatus());
        response.setReportType(report.getReportType());
        response.setUserCode(report.getUserCode());
        response.setSemesterId(report.getSemester().getId());
        return response;
    }


}

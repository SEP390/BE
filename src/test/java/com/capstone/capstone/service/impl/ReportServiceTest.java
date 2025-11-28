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
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.Report;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.ReportRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReportService reportService;

    // ---------------------------------------------------------
    // createReport
    // ---------------------------------------------------------

    // ✅ TC1: Tạo report thành công với dữ liệu hợp lệ
    @Test
    void createReport_shouldCreateSuccessfully_whenValidRequest() {
        UUID currentUserId = UUID.randomUUID();

        User user = new User();
        user.setId(currentUserId);
        user.setUserCode("EMP001");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(user);

        CreateReportRequest req = new CreateReportRequest();
        req.setContent("Thiết bị hỏng ở phòng A101");
        req.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));
            when(reportRepository.save(any(Report.class))).thenAnswer(inv -> {
                Report r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

            // Act
            CreateReportResponse resp = reportService.createReport(req);

            // Assert – verify save
            ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
            verify(reportRepository, times(1)).save(captor.capture());
            Report saved = captor.getValue();

            assertEquals("Thiết bị hỏng ở phòng A101", saved.getContent());
            assertEquals(ReportTypeEnum.MAINTENANCE_REQUEST, saved.getReportType());
            assertEquals(ReportStatusEnum.PENDING, saved.getReportStatus());
            assertEquals("EMP001", saved.getUserCode());
            assertEquals(employee, saved.getEmployee());
            assertNotNull(saved.getCreatedAt());

            // Assert – response mapping
            assertNotNull(resp.getReportId());
            assertEquals("Thiết bị hỏng ở phòng A101", resp.getContent());
            assertEquals(ReportTypeEnum.MAINTENANCE_REQUEST, resp.getReportType());
            assertEquals(ReportStatusEnum.PENDING, resp.getReportStatus());
            assertEquals("EMP001", resp.getUserCode());
            assertNotNull(resp.getCreatedAt());
        }
    }

    // ❌ TC2: User hiện tại không tồn tại → NotFoundException
    @Test
    void createReport_shouldThrowNotFound_whenCurrentUserNotFound() {
        UUID currentUserId = UUID.randomUUID();

        CreateReportRequest req = new CreateReportRequest();
        req.setContent("Test");
        req.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> reportService.createReport(req)
            );

            assertEquals("User not found", ex.getMessage());
            verifyNoInteractions(employeeRepository, reportRepository);
        }
    }

    // ❌ TC3: User không có employee → NotFoundException
    @Test
    void createReport_shouldThrowNotFound_whenEmployeeNotFound() {
        UUID currentUserId = UUID.randomUUID();

        User user = new User();
        user.setId(currentUserId);
        user.setUserCode("EMP001");

        CreateReportRequest req = new CreateReportRequest();
        req.setContent("Test");
        req.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(employeeRepository.findByUser(user)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> reportService.createReport(req)
            );

            assertEquals("Employee not found", ex.getMessage());
            verify(reportRepository, never()).save(any());
        }
    }

    // ✅ TC4: Content null/blank → IllegalArgumentException, không save
    @Test
    void createReport_shouldRejectBlankContent() {
        UUID currentUserId = UUID.randomUUID();

        User user = new User();
        user.setId(currentUserId);
        user.setUserCode("EMP001");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(user);

        CreateReportRequest req = new CreateReportRequest();
        req.setContent("   "); // blank
        req.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> reportService.createReport(req)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("content"));
            verify(reportRepository, never()).save(any());
        }
    }

    // ✅ TC5: ReportType null → IllegalArgumentException, không save
    @Test
    void createReport_shouldRejectNullReportType() {
        UUID currentUserId = UUID.randomUUID();

        User user = new User();
        user.setId(currentUserId);
        user.setUserCode("EMP001");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(user);

        CreateReportRequest req = new CreateReportRequest();
        req.setContent("Nội dung hợp lệ");
        req.setReportType(null);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> reportService.createReport(req)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("report type"));
            verify(reportRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------
    // getAllReports
    // ---------------------------------------------------------

    // ✅ TC6: MANAGER thấy tất cả report
    @Test
    void getAllReports_shouldReturnAll_whenUserIsManager() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User empUser = new User();
        empUser.setId(UUID.randomUUID());
        empUser.setFullName("Nhân viên A");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(empUser);

        Report r1 = new Report();
        r1.setId(UUID.randomUUID());
        r1.setEmployee(employee);
        r1.setContent("Report 1");
        r1.setResponseMessage("Resp 1");
        r1.setReportStatus(ReportStatusEnum.PENDING);
        r1.setCreatedAt(LocalDateTime.now().minusDays(1));
        r1.setUserCode("EMP001");
        r1.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        Report r2 = new Report();
        r2.setId(UUID.randomUUID());
        r2.setEmployee(employee);
        r2.setContent("Report 2");
        r2.setResponseMessage("Resp 2");
        r2.setReportStatus(ReportStatusEnum.PENDING);
        r2.setCreatedAt(LocalDateTime.now());
        r2.setUserCode("EMP002");
        r2.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(reportRepository.findAll()).thenReturn(List.of(r1, r2));

            List<GetAllReportResponse> result = reportService.getAllReports();

            verify(reportRepository, times(1)).findAll();
            assertEquals(2, result.size());

            GetAllReportResponse res1 = result.get(0);
            assertEquals(r1.getId(), res1.getReportId());
            assertEquals(employee.getId(), res1.getEmployeeId());
            assertEquals("Report 1", res1.getContent());
            assertEquals("Resp 1", res1.getResponseMessage());
            assertEquals("EMP001", res1.getUserCode());
            assertEquals("Nhân viên A", res1.getEmployeeName());
        }
    }

    // ✅ TC7: ADMIN cũng thấy tất cả report
    @Test
    void getAllReports_shouldReturnAll_whenUserIsAdmin() {
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(RoleEnum.ADMIN);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(adminId);

            List<GetAllReportResponse> result = reportService.getAllReports();

            verify(reportRepository, times(1)).findAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ✅ TC8: TECHNICAL chỉ thấy report MAINTENANCE_REQUEST
    @Test
    void getAllReports_shouldReturnMaintenance_whenUserIsTechnical() {
        UUID techId = UUID.randomUUID();
        User tech = new User();
        tech.setId(techId);
        tech.setRole(RoleEnum.TECHNICAL);

        User empUser = new User();
        empUser.setFullName("Tech Staff");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(empUser);

        Report r = new Report();
        r.setId(UUID.randomUUID());
        r.setEmployee(employee);
        r.setContent("Sửa điều hòa");
        r.setReportStatus(ReportStatusEnum.PENDING);
        r.setCreatedAt(LocalDateTime.now());
        r.setUserCode("EMP003");
        r.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(techId);

            when(userRepository.findById(techId)).thenReturn(Optional.of(tech));
            when(reportRepository.findByReportType(ReportTypeEnum.MAINTENANCE_REQUEST))
                    .thenReturn(List.of(r));

            List<GetAllReportResponse> result = reportService.getAllReports();

            verify(reportRepository, times(1))
                    .findByReportType(ReportTypeEnum.MAINTENANCE_REQUEST);
            assertEquals(1, result.size());
            assertEquals("Sửa điều hòa", result.get(0).getContent());
            assertEquals("Tech Staff", result.get(0).getEmployeeName());
        }
    }

    // ✅ TC9: GUARD chỉ thấy report của chính mình
    @Test
    void getAllReports_shouldReturnOwnReports_whenUserIsGuard() {
        UUID guardId = UUID.randomUUID();
        User guardUser = new User();
        guardUser.setId(guardId);
        guardUser.setRole(RoleEnum.GUARD);
        guardUser.setFullName("Bảo vệ 1");

        Employee guardEmp = new Employee();
        guardEmp.setId(UUID.randomUUID());
        guardEmp.setUser(guardUser);

        Report r = new Report();
        r.setId(UUID.randomUUID());
        r.setEmployee(guardEmp);
        r.setContent("Báo cáo ca trực");
        r.setReportStatus(ReportStatusEnum.PENDING);
        r.setCreatedAt(LocalDateTime.now());
        r.setUserCode("G001");
        r.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardId);

            when(userRepository.findById(guardId)).thenReturn(Optional.of(guardUser));
            when(employeeRepository.findByUser(guardUser)).thenReturn(Optional.of(guardEmp));
            when(reportRepository.findByEmployeeId(guardEmp.getId()))
                    .thenReturn(List.of(r));

            List<GetAllReportResponse> result = reportService.getAllReports();

            verify(reportRepository, times(1)).findByEmployeeId(guardEmp.getId());
            assertEquals(1, result.size());
            assertEquals("Báo cáo ca trực", result.get(0).getContent());
            assertEquals("Bảo vệ 1", result.get(0).getEmployeeName());
        }
    }

    // ❌ TC10: role không được phép (RESIDENT) → AccessDeniedException
    @Test
    void getAllReports_shouldThrowAccessDenied_whenForbiddenRole() {
        UUID userId = UUID.randomUUID();
        User u = new User();
        u.setId(userId);
        u.setRole(RoleEnum.RESIDENT);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(u));

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> reportService.getAllReports()
            );

            assertEquals("Forbidden", ex.getMessage());
            verify(reportRepository, never()).findAll();
        }
    }

    // ❌ TC11: current user không tồn tại → NotFoundException
    @Test
    void getAllReports_shouldThrowNotFound_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> reportService.getAllReports()
            );

            assertEquals("User not found", ex.getMessage());
            verifyNoInteractions(reportRepository);
        }
    }

    // ---------------------------------------------------------
    // updateReport
    // ---------------------------------------------------------

    // ✅ TC12: MANAGER update report thành công
    @Test
    void updateReport_shouldUpdateSuccessfully_whenUserIsManager() {
        UUID reportId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        Employee managerEmp = new Employee();
        managerEmp.setId(UUID.randomUUID());
        managerEmp.setUser(manager);

        User empUser = new User();
        empUser.setId(UUID.randomUUID());
        empUser.setFullName("Nhân viên 1");

        Employee reportOwner = new Employee();
        reportOwner.setId(UUID.randomUUID());
        reportOwner.setUser(empUser);

        Report report = new Report();
        report.setId(reportId);
        report.setEmployee(reportOwner);
        report.setContent("Nội dung cũ");
        report.setReportStatus(ReportStatusEnum.PENDING);
        report.setCreatedAt(LocalDateTime.now().minusDays(1));
        report.setUserCode("EMP001");
        report.setResponseMessage(null);

        UpdateReportRequest req = new UpdateReportRequest();
        req.setResponseMessage("Đã xử lý");
        req.setReportStatus(ReportStatusEnum.PENDING);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findByUser(manager)).thenReturn(Optional.of(managerEmp));
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateReportResponse resp = reportService.updateReport(reportId, req);

            verify(reportRepository, times(1)).save(report);
            assertEquals("Đã xử lý", report.getResponseMessage());
            assertEquals(ReportStatusEnum.PENDING, report.getReportStatus());

            assertEquals(reportId, resp.getReportId());
            assertEquals(reportOwner.getId(), resp.getEmployeeId());
            assertEquals("Nội dung cũ", resp.getContent());
            assertEquals("EMP001", resp.getUserCode());
        }
    }

    // ✅ TC13: GUARD update chính report của mình (trùng employee object) → OK
    @Test
    void updateReport_shouldAllowOwnerGuard_whenSameEmployeeInstance() {
        UUID reportId = UUID.randomUUID();
        UUID guardId = UUID.randomUUID();

        User guard = new User();
        guard.setId(guardId);
        guard.setRole(RoleEnum.GUARD);
        guard.setFullName("Bảo vệ 1");

        Employee emp = new Employee();
        emp.setId(UUID.randomUUID());
        emp.setUser(guard);

        // Report dùng chung cùng instance employee → điều kiện == sẽ true
        Report report = new Report();
        report.setId(reportId);
        report.setEmployee(emp);
        report.setContent("Nội dung báo cáo");
        report.setReportStatus(ReportStatusEnum.PENDING);
        report.setCreatedAt(LocalDateTime.now().minusDays(1));
        report.setUserCode("G001");

        UpdateReportRequest req = new UpdateReportRequest();
        req.setResponseMessage("Tự xử lý xong");
        req.setReportStatus(ReportStatusEnum.PENDING);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardId);

            when(userRepository.findById(guardId)).thenReturn(Optional.of(guard));
            when(employeeRepository.findByUser(guard)).thenReturn(Optional.of(emp));
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateReportResponse resp = reportService.updateReport(reportId, req);

            verify(reportRepository, times(1)).save(report);
            assertEquals("Tự xử lý xong", report.getResponseMessage());
            assertEquals(ReportStatusEnum.PENDING, resp.getReportStatus());
        }
    }

    // ❌ TC14: reportId không tồn tại → NotFoundException
    @Test
    void updateReport_shouldThrowNotFound_whenReportNotFound() {
        UUID reportId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(RoleEnum.MANAGER);

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(user);

        UpdateReportRequest req = new UpdateReportRequest();
        req.setResponseMessage("Test");
        req.setReportStatus(ReportStatusEnum.PENDING);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(employeeRepository.findByUser(user)).thenReturn(Optional.of(employee));
            when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> reportService.updateReport(reportId, req)
            );

            assertEquals("Report not found", ex.getMessage());
            verify(reportRepository, never()).save(any());
        }
    }

    // ❌ TC15: role không hợp lệ / không phải owner / không phải MANAGER/TECHNICAL → AccessDeniedException
    @Test
    void updateReport_shouldThrowAccessDenied_whenForbiddenRoleAndNotOwner() {
        UUID reportId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setRole(RoleEnum.RESIDENT);

        Employee emp = new Employee();
        emp.setId(UUID.randomUUID());
        emp.setUser(user);

        // report employee khác
        Employee anotherEmp = new Employee();
        anotherEmp.setId(UUID.randomUUID());

        Report report = new Report();
        report.setId(reportId);
        report.setEmployee(anotherEmp);

        UpdateReportRequest req = new UpdateReportRequest();
        req.setResponseMessage("Không được phép");
        req.setReportStatus(ReportStatusEnum.PENDING);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(employeeRepository.findByUser(user)).thenReturn(Optional.of(emp));
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> reportService.updateReport(reportId, req)
            );

            assertEquals("Access denied", ex.getMessage());
            verify(reportRepository, never()).save(any());
        }
    }

    // ⚠️ TC16 (REALISTIC BUG): GUARD là owner nhưng id UUID bằng nhau mà không cùng object → hiện tại sẽ bị AccessDenied vì dùng ==
    // Mong muốn: được phép update, nhưng service hiện tại dùng "==" nên điều kiện thất bại → test này sẽ FAIL.
    @Test
    void updateReport_shouldAllowOwnerGuard_whenEmployeeIdEquals_butServiceUsesDoubleEquals_bug() {
        UUID reportId = UUID.randomUUID();
        UUID guardId = UUID.randomUUID();

        UUID employeeIdValue = UUID.randomUUID();

        User guard = new User();
        guard.setId(guardId);
        guard.setRole(RoleEnum.GUARD);

        Employee empFromUser = new Employee();
        empFromUser.setId(employeeIdValue);
        empFromUser.setUser(guard);

        Employee empInReport = new Employee();
        empInReport.setId(UUID.fromString(employeeIdValue.toString())); // value bằng nhau nhưng object khác

        Report report = new Report();
        report.setId(reportId);
        report.setEmployee(empInReport);
        report.setReportStatus(ReportStatusEnum.PENDING);

        UpdateReportRequest req = new UpdateReportRequest();
        req.setResponseMessage("Owner update");
        req.setReportStatus(ReportStatusEnum.PENDING);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardId);

            when(userRepository.findById(guardId)).thenReturn(Optional.of(guard));
            when(employeeRepository.findByUser(guard)).thenReturn(Optional.of(empFromUser));
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

            // Mong muốn: KHÔNG ném exception (vì id bằng nhau)
            // Hiện tại: sẽ ném AccessDeniedException → test FAIL để lộ bug.
            UpdateReportResponse resp = reportService.updateReport(reportId, req);

            assertEquals("Owner update", resp.getResponseMessage());
        }
    }

    // ---------------------------------------------------------
    // getReportById
    // ---------------------------------------------------------

    // ✅ TC17: getReportById – map đúng dữ liệu
    @Test
    void getReportById_shouldReturnMappedResponse_whenReportExists() {
        UUID reportId = UUID.randomUUID();

        User empUser = new User();
        empUser.setId(UUID.randomUUID());
        empUser.setFullName("Nhân viên 1");

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setUser(empUser);

        Report report = new Report();
        report.setId(reportId);
        report.setEmployee(employee);
        report.setContent("Nội dung báo cáo");
        report.setResponseMessage("Đã tiếp nhận");
        report.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        report.setReportStatus(ReportStatusEnum.PENDING);
        report.setReportType(ReportTypeEnum.MAINTENANCE_REQUEST);
        report.setUserCode("EMP001");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        GetReportByIdResponse resp = reportService.getReportById(reportId);

        verify(reportRepository, times(1)).findById(reportId);
        assertEquals(reportId, resp.getReportId());
        assertEquals(employee.getId(), resp.getEmployeeId());
        assertEquals("Nhân viên 1", resp.getEmployeeName());
        assertEquals("Nội dung báo cáo", resp.getContent());
        assertEquals("Đã tiếp nhận", resp.getResponseMessage());
        assertEquals(report.getCreatedAt(), resp.getCreatedDate());
        assertEquals(ReportStatusEnum.PENDING, resp.getReportStatus());
        assertEquals(ReportTypeEnum.MAINTENANCE_REQUEST, resp.getReportType());
        assertEquals("EMP001", resp.getUserCode());
    }

    // ❌ TC18: getReportById – không tồn tại report → NotFoundException
    @Test
    void getReportById_shouldThrowNotFound_whenReportNotFound() {
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> reportService.getReportById(reportId)
        );

        assertEquals("Report not found", ex.getMessage());
        verify(reportRepository, times(1)).findById(reportId);
    }
}
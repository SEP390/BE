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
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.MockedStatic;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    // ======================================================
    // =============== CREATE EMPLOYEE ======================
    // ======================================================

    // 🎯 TC1: MANAGER tạo employee GUARD với dữ liệu hợp lệ → thành công
    @Test
    void createEmployee_shouldCreateSuccessfully_whenManagerWithValidGuardRequest() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("guard01");
            req.setEmail("guard01@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);
            req.setHireDate(LocalDate.of(2024, 1, 1));
            req.setContractEndDate(LocalDate.of(2024, 12, 31));

            Employee savedEmp = new Employee();
            savedEmp.setId(UUID.randomUUID());
            when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmp);

            CreateEmployeeResponse resp = employeeService.createEmployee(req);

            assertNotNull(resp);
            assertEquals("guard01", resp.getUsername());
            assertEquals("guard01@example.com", resp.getEmail());
            assertEquals(RoleEnum.GUARD, resp.getRole());
            assertNotNull(resp.getEmployeeId());

            verify(userRepository, times(1)).save(any(User.class));
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }
    }

    // 🎯 TC2: Current user không phải MANAGER → không được tạo employee
    @Test
    void createEmployee_shouldReject_whenCurrentUserIsNotManager() {
        UUID userId = UUID.randomUUID();
        User normalUser = new User();
        normalUser.setId(userId);
        normalUser.setRole(RoleEnum.GUARD);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("emp01");
            req.setEmail("emp01@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertTrue(ex.getMessage().toLowerCase().contains("permission"));
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    // 🎯 TC3: Email null/blank → ném lỗi "Email is required"
    @Test
    void createEmployee_shouldReject_whenEmailIsNullOrBlank() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("emp01");
            req.setEmail("  "); // blank
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Email is required", ex.getMessage());
        }
    }

    // 🎯 TC4: Username null/blank → ném lỗi "Username is required"
    @Test
    void createEmployee_shouldReject_whenUsernameIsNullOrBlank() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("   ");
            req.setEmail("emp01@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Username is required", ex.getMessage());
        }
    }

    // 🎯 TC5: Username bị trùng trong hệ thống → ném lỗi "Username is already taken"
    @Test
    void createEmployee_shouldReject_whenUsernameAlreadyExists() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User existed = new User();
        existed.setUsername("dupUser");

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of(existed));

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("dupUser");
            req.setEmail("new@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Username is already taken", ex.getMessage());
        }
    }

    // 🎯 TC6: Email bị trùng → ném lỗi "Email is already taken"
    @Test
    void createEmployee_shouldReject_whenEmailAlreadyExists() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User existed = new User();
        existed.setEmail("dup@example.com");

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of(existed));

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newuser");
            req.setEmail("dup@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Email is already taken", ex.getMessage());
        }
    }

    // 🎯 TC7: Email sai format → ném "Invalid email format"
    @Test
    void createEmployee_shouldReject_whenEmailFormatInvalid() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("u1");
            req.setEmail("invalid-email-format");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Invalid email format", ex.getMessage());
        }
    }

    // 🎯 TC8: Role không phải GUARD/CLEANER → ném lỗi "Employee role must be GUARD or CLEANER"
    @Test
    void createEmployee_shouldReject_whenRoleNotGuardOrCleaner() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("abc");
            req.setEmail("abc@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.MANAGER); // invalid cho Employee

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Employee role must be GUARD or CLEANER", ex.getMessage());
        }
    }

    // 🎯 TC9: contractEndDate < hireDate → ném "Contract end date must be after hire date"
    @Test
    void createEmployee_shouldReject_whenContractEndBeforeHireDate() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("emp");
            req.setEmail("emp@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);
            req.setHireDate(LocalDate.of(2024, 5, 10));
            req.setContractEndDate(LocalDate.of(2024, 5, 9));

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Contract end date must be after hire date", ex.getMessage());
        }
    }

    // 🎯 TC10: Password null/blank → ném "Password is required"
    @Test
    void createEmployee_shouldReject_whenPasswordBlank() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("emp");
            req.setEmail("emp@example.com");
            req.setPassword("  ");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Password is required", ex.getMessage());
        }
    }

    // 🎯 TC11: Password < 6 ký tự → ném "Password must be at least 6 characters"
    @Test
    void createEmployee_shouldReject_whenPasswordTooShort() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("emp");
            req.setEmail("emp@example.com");
            req.setPassword("123"); // too short
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );
            assertEquals("Password must be at least 6 characters", ex.getMessage());
        }
    }

    // ======================================================
    // =============== GET ALL EMPLOYEE =====================
    // ======================================================

    // 🎯 TC12: MANAGER được phép getAllEmployee, mapping đủ dữ liệu
    @Test
    void getAllEmployee_shouldReturnList_whenManager() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        Employee e1 = new Employee();
        e1.setId(UUID.randomUUID());
        User u1 = new User();
        u1.setUsername("guard01");
        u1.setRole(RoleEnum.GUARD);
        u1.setPhoneNumber("0123");
        u1.setEmail("g1@example.com");
        e1.setUser(u1);

        Employee e2 = new Employee();
        e2.setId(UUID.randomUUID());
        e2.setUser(null); // case user null

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

            List<GetAllEmployeeResponse> res = employeeService.getAllEmployee();

            assertEquals(2, res.size());

            GetAllEmployeeResponse r1 = res.get(0);
            assertEquals(e1.getId(), r1.getEmployeeId());
            assertEquals("guard01", r1.getUsername());
            assertEquals(RoleEnum.GUARD, r1.getRole());
            assertEquals("0123", r1.getPhone());
            assertEquals("g1@example.com", r1.getEmail());

            GetAllEmployeeResponse r2 = res.get(1);
            assertEquals(e2.getId(), r2.getEmployeeId());
            assertNull(r2.getUsername());
            assertNull(r2.getRole());
        }
    }

    // 🎯 TC13: User không phải MANAGER → không được phép getAllEmployee
    @Test
    void getAllEmployee_shouldReject_whenCurrentUserNotManager() {
        UUID userId = UUID.randomUUID();
        User normal = new User();
        normal.setId(userId);
        normal.setRole(RoleEnum.GUARD);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(normal));

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.getAllEmployee()
            );
            assertTrue(ex.getMessage().contains("permission"));
            verify(employeeRepository, never()).findAll();
        }
    }

    // ======================================================
    // =============== GET EMPLOYEE BY ID ===================
    // ======================================================

    // 🎯 TC14: getEmployeeById → trả đúng data khi employee tồn tại
    @Test
    void getEmployeeById_shouldReturnData_whenEmployeeExists() {
        UUID empId = UUID.randomUUID();
        Employee e = new Employee();
        e.setId(empId);

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUserCode("SE99999");
        u.setDob(LocalDate.of(2000, 1, 1));
        u.setEmail("emp@example.com");
        u.setRole(RoleEnum.GUARD);
        u.setGender(null);
        u.setFullName("Guard One");
        u.setPhoneNumber("0909");
        e.setUser(u);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(e));

        GetEmployeeByIdResponse res = employeeService.getEmployeeById(empId);

        assertEquals(empId, res.getEmployeeId());
        assertEquals(u.getId(), res.getUserId());
        assertEquals(u.getUserCode(), res.getUserCode());
        assertEquals(u.getEmail(), res.getEmail());
        assertEquals(u.getFullName(), res.getFullName());
        assertEquals(u.getPhoneNumber(), res.getPhoneNumber());
    }

    // 🎯 TC15: getEmployeeById → employee không tồn tại → NotFoundException
    @Test
    void getEmployeeById_shouldThrowNotFound_whenEmployeeNotExist() {
        UUID empId = UUID.randomUUID();
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.getEmployeeById(empId));
    }

    // ======================================================
    // =============== UPDATE EMPLOYEE ======================
    // ======================================================

    // 🎯 TC16: MANAGER update employee với role GUARD/CLEANER hợp lệ → OK
    @Test
    void updateEmployee_shouldUpdate_whenManagerWithValidData() {
        UUID managerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User empUser = new User();
        empUser.setId(UUID.randomUUID());
        empUser.setFullName("Guard1");

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);
        employee.setHireDate(LocalDate.of(2024, 1, 1));

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setPhoneNumber("0999");
            req.setBirthDate(LocalDate.of(2000, 1, 1));
            req.setRole(RoleEnum.CLEANER);
            req.setContractEndDate(LocalDate.of(2024, 12, 31));

            UpdateEmployeeResponse res = employeeService.updateEmployee(empId, req);

            assertEquals(empId, res.getEmployeeId());
            assertEquals(empUser.getFullName(), res.getFullName());
            assertEquals(req.getBirthDate(), res.getBirthDate());
            assertEquals(req.getContractEndDate(), res.getContractEndDate());

            assertEquals("0999", empUser.getPhoneNumber());
            assertEquals(RoleEnum.CLEANER, empUser.getRole());
            verify(userRepository).save(empUser);
            verify(employeeRepository).save(employee);
        }
    }

    // 🎯 TC17: Owner (chính employee đó) được tự update thông tin của mình
    @Test
    void updateEmployee_shouldAllowOwnerToUpdateSelf() {
        UUID empUserId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User current = new User();
        current.setId(empUserId);
        current.setRole(RoleEnum.GUARD);  // không phải MANAGER nhưng là owner

        User empUser = current; // cùng user

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);
        employee.setHireDate(LocalDate.of(2024, 1, 1));

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(empUserId);
            when(userRepository.findById(empUserId)).thenReturn(Optional.of(current));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setPhoneNumber("0888");
            req.setBirthDate(LocalDate.of(1999, 1, 1));
            req.setRole(RoleEnum.GUARD); // vẫn guard
            req.setContractEndDate(LocalDate.of(2024, 12, 31));

            UpdateEmployeeResponse res = employeeService.updateEmployee(empId, req);

            assertEquals(empId, res.getEmployeeId());
            assertEquals("0888", empUser.getPhoneNumber());
            verify(userRepository).save(empUser);
        }
    }

    // 🎯 TC18: Người không phải MANAGER & không phải owner → không được update
    @Test
    void updateEmployee_shouldReject_whenNotManagerAndNotOwner() {
        UUID currentId = UUID.randomUUID();
        UUID empUserId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User current = new User();
        current.setId(currentId);
        current.setRole(RoleEnum.GUARD);

        User empUser = new User();
        empUser.setId(empUserId);

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(currentId);
            when(userRepository.findById(currentId)).thenReturn(Optional.of(current));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.updateEmployee(empId, req)
            );
            assertTrue(ex.getMessage().contains("not allowed"));
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    // 🎯 TC19: Update role khác GUARD/CLEANER → reject
    @Test
    void updateEmployee_shouldReject_whenRoleNotGuardOrCleaner() {
        UUID managerId = UUID.randomUUID();
        UUID empId   = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User empUser = new User();
        empUser.setId(UUID.randomUUID());

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);
        employee.setHireDate(LocalDate.of(2024, 1, 1));

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setRole(RoleEnum.MANAGER); // invalid

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.updateEmployee(empId, req)
            );
            assertTrue(ex.getMessage().contains("GUARD or CLEANER"));
        }
    }

    // 🎯 TC20: contractEndDate < hireDate khi update → reject
    @Test
    void updateEmployee_shouldReject_whenContractEndBeforeHireDate() {
        UUID managerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User empUser = new User();
        empUser.setId(UUID.randomUUID());

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);
        employee.setHireDate(LocalDate.of(2024, 5, 10));

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setRole(RoleEnum.GUARD);
            req.setContractEndDate(LocalDate.of(2024, 5, 9));

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.updateEmployee(empId, req)
            );
            assertEquals("Contract end date must be after hire date", ex.getMessage());
        }
    }

    // ======================================================
    // =============== RESET PASSWORD =======================
    // ======================================================

    // 🎯 TC21: MANAGER reset password cho employee → OK
    @Test
    void resetPassword_shouldSucceed_whenManagerResets() {
        UUID managerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User empUser = new User();
        empUser.setId(UUID.randomUUID());

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
            when(passwordEncoder.encode("123456")).thenReturn("encoded");

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("123456");

            ResetPasswordResponse res = employeeService.resetPassword(empId, req);

            assertEquals(empId, res.getEmployeeId());
            verify(userRepository).save(empUser);
        }
    }

    // 🎯 TC22: Owner tự reset password cho mình → OK
    @Test
    void resetPassword_shouldAllowOwnerToResetOwnPassword() {
        UUID userId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User current = new User();
        current.setId(userId);
        current.setRole(RoleEnum.GUARD);

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(current); // chính là owner

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(current));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));
            when(passwordEncoder.encode("654321")).thenReturn("encoded2");

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("654321");

            ResetPasswordResponse res = employeeService.resetPassword(empId, req);

            assertEquals(empId, res.getEmployeeId());
            verify(userRepository).save(current);
        }
    }

    // 🎯 TC23: Không phải MANAGER & không phải owner → không được reset password
    @Test
    void resetPassword_shouldReject_whenNotManagerAndNotOwner() {
        UUID curId = UUID.randomUUID();
        UUID empUserId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User current = new User();
        current.setId(curId);
        current.setRole(RoleEnum.GUARD);

        User empUser = new User();
        empUser.setId(empUserId);

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(empUser);

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(curId);
            when(userRepository.findById(curId)).thenReturn(Optional.of(current));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("123456");

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.resetPassword(empId, req)
            );
            assertTrue(ex.getMessage().contains("not allowed"));
            verify(userRepository, never()).save(any());
        }
    }

    // 🎯 TC24: newPassword null/blank → "Password is required"
    @Test
    void resetPassword_shouldReject_whenPasswordBlank() {
        UUID managerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(new User());

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("  ");

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.resetPassword(empId, req)
            );
            assertEquals("Password is required", ex.getMessage());
        }
    }

    // 🎯 TC25: newPassword < 6 ký tự → "Password must be at least 6 characters"
    @Test
    void resetPassword_shouldReject_whenPasswordTooShort() {
        UUID managerId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        Employee employee = new Employee();
        employee.setId(empId);
        employee.setUser(new User());

        try (MockedStatic<AuthenUtil> mocked = mockStatic(AuthenUtil.class)) {
            mocked.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(empId)).thenReturn(Optional.of(employee));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("123"); // too short

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.resetPassword(empId, req)
            );
            assertEquals("Password must be at least 6 characters", ex.getMessage());
        }
    }
}
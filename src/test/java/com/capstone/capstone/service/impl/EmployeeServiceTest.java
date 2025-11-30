package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.employee.CreateEmployeeRequest;
import com.capstone.capstone.dto.request.employee.ResetPasswordRequest;
import com.capstone.capstone.dto.request.employee.UpdateEmployeeRequest;
import com.capstone.capstone.dto.response.employee.CreateEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetAllEmployeeResponse;
import com.capstone.capstone.dto.response.employee.GetEmployeeByIdResponse;
import com.capstone.capstone.dto.response.employee.ResetPasswordResponse;
import com.capstone.capstone.dto.response.employee.UpdateEmployeeResponse;
import com.capstone.capstone.entity.Employee;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    EmployeeService employeeService;

    // --------------------------------------------------
    // createEmployee
    // --------------------------------------------------

    /**
     * 🎯 TC1 – Tạo employee thành công:
     *  - currentUser là MANAGER
     *  - username/email unique, format email hợp lệ
     *  - role là GUARD/CLEANER
     *  - hireDate <= contractEndDate
     *  - password hợp lệ (>= 6 ký tự)
     *  Kỳ vọng:
     *  - userRepository.save & employeeRepository.save được gọi
     *  - password được encode
     *  - response mapping đúng dữ liệu.
     */
    @Test
    void createEmployee_shouldCreateSuccessfully_whenManagerAndValidRequest() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            // Arrange
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of()); // không có user trùng
            when(passwordEncoder.encode("strongPass")).thenReturn("encodedPass");

            UUID generatedEmployeeId = UUID.randomUUID();
            when(employeeRepository.save(any(Employee.class)))
                    .thenAnswer(invocation -> {
                        Employee e = invocation.getArgument(0);
                        e.setId(generatedEmployeeId);
                        return e;
                    });

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newEmp");
            req.setEmail("emp@example.com");
            req.setPassword("strongPass");
            req.setRole(RoleEnum.GUARD);
            req.setDob(LocalDate.of(2000, 1, 1));
            req.setUserCode("EMP001");
            req.setGender(GenderEnum.MALE);
            req.setPhoneNumber("0123456789");
            req.setFullName("Employee Name");
            req.setImage("img-url");
            req.setHireDate(LocalDate.of(2024, 1, 1));
            req.setContractEndDate(LocalDate.of(2025, 1, 1));

            // Act
            CreateEmployeeResponse resp = employeeService.createEmployee(req);

            // Assert
            verify(userRepository).findById(managerId);
            verify(userRepository).findAll();
            verify(passwordEncoder).encode("strongPass");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals("newEmp", savedUser.getUsername());
            assertEquals("encodedPass", savedUser.getPassword());
            assertEquals(RoleEnum.GUARD, savedUser.getRole());

            ArgumentCaptor<Employee> empCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(empCaptor.capture());
            Employee savedEmp = empCaptor.getValue();
            assertEquals(savedUser, savedEmp.getUser());
            assertEquals(LocalDate.of(2024, 1, 1), savedEmp.getHireDate());
            assertEquals(LocalDate.of(2025, 1, 1), savedEmp.getContractEndDate());

            assertNotNull(resp);
            assertEquals("emp@example.com", resp.getEmail());
            assertEquals("newEmp", resp.getUsername());
            assertEquals(RoleEnum.GUARD, resp.getRole());
            assertEquals(generatedEmployeeId, resp.getEmployeeId());
        }
    }

    /**
     * 🎯 TC2 – currentUser không phải MANAGER:
     *  - GUARD/GUARD, CLEANER,… gọi API tạo employee
     *  Kỳ vọng:
     *  - Ném BadHttpRequestException với message permission
     *  - Không gọi userRepository.findAll, save, employeeRepository.save.
     */
    @Test
    void createEmployee_shouldThrow_whenCurrentUserIsNotManager() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID guardId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardId);

            User guard = new User();
            guard.setId(guardId);
            guard.setRole(RoleEnum.GUARD);

            when(userRepository.findById(guardId)).thenReturn(Optional.of(guard));

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("any");
            req.setEmail("any@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("permission"));

            verify(userRepository).findById(guardId);
            verify(userRepository, never()).findAll();
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC3 – Email rỗng/null:
     *  - currentUser MANAGER
     *  - request.email = null / "" -> nên reject trước khi làm gì thêm
     *  Kỳ vọng:
     *  - Ném BadHttpRequestException "Email is required"
     *  - Không lưu user/employee.
     */
    @Test
    void createEmployee_shouldThrow_whenEmailMissing() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newEmp");
            req.setEmail("  "); // blank
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertEquals("Email is required", ex.getMessage());
            verify(userRepository).findById(managerId);
            verify(userRepository).findAll(); // vẫn bị gọi theo code hiện tại
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC4 – Username trùng với user đã tồn tại:
     *  - currentUser MANAGER
     *  - username đã tồn tại trong list userRepository.findAll()
     *  Kỳ vọng:
     *  - Ném BadHttpRequestException "Username is already taken"
     *  - Không lưu user/employee mới.
     */
    @Test
    void createEmployee_shouldThrow_whenUsernameAlreadyTaken() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));

            User existing = new User();
            existing.setUsername("dupUser");
            existing.setEmail("old@example.com");
            when(userRepository.findAll()).thenReturn(List.of(existing));

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
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC5 – Email trùng với user đã tồn tại.
     */
    @Test
    void createEmployee_shouldThrow_whenEmailAlreadyTaken() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));

            User existing = new User();
            existing.setUsername("old");
            existing.setEmail("dup@example.com");
            when(userRepository.findAll()).thenReturn(List.of(existing));

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newUser");
            req.setEmail("dup@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertEquals("Email is already taken", ex.getMessage());
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC6 – Email format sai.
     */
    @Test
    void createEmployee_shouldThrow_whenEmailFormatInvalid() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newUser");
            req.setEmail("invalid-email"); // không có @
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertEquals("Invalid email format", ex.getMessage());
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC7 – Role không phải GUARD/CLEANER/TECHNICAL:
     *  - currentUser: MANAGER
     *  - request.role = RESIDENT (không thuộc GUARD/CLEANER/TECHNICAL)
     *  Kỳ vọng theo service hiện tại:
     *  - Ném BadHttpRequestException với message:
     *    "You only can create account for GUARD, CLEANER or TECHNICAL"
     *  - Không save user/employee.
     */
    @Test
    void createEmployee_shouldThrow_whenRoleIsNotGuardCleanerOrTechnical() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newUser");
            req.setEmail("ok@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.RESIDENT); // ❌ không hợp lệ theo service

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertEquals("You only can create account for GUARD, CLEANER or TECHNICAL", ex.getMessage());
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC8 – ContractEndDate < HireDate.
     */
    @Test
    void createEmployee_shouldThrow_whenContractEndBeforeHireDate() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newUser");
            req.setEmail("ok@example.com");
            req.setPassword("123456");
            req.setRole(RoleEnum.GUARD);
            req.setHireDate(LocalDate.of(2025, 1, 1));
            req.setContractEndDate(LocalDate.of(2024, 1, 1)); // trước hire date

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertTrue(ex.getMessage().contains("Contract end date must be after hire date"));
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC9 – Password null/blank.
     */
    @Test
    void createEmployee_shouldThrow_whenPasswordMissing() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newUser");
            req.setEmail("ok@example.com");
            req.setPassword("   "); // blank
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertEquals("Password is required", ex.getMessage());
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC10 – Password < 6 ký tự.
     */
    @Test
    void createEmployee_shouldThrow_whenPasswordTooShort() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(userRepository.findAll()).thenReturn(List.of());

            CreateEmployeeRequest req = new CreateEmployeeRequest();
            req.setUsername("newUser");
            req.setEmail("ok@example.com");
            req.setPassword("123"); // ngắn
            req.setRole(RoleEnum.GUARD);

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.createEmployee(req)
            );

            assertEquals("Password must be at least 6 characters", ex.getMessage());
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    // --------------------------------------------------
    // getAllEmployee
    // --------------------------------------------------

    /**
     * 🎯 TC11 – MANAGER lấy danh sách employee thành công:
     *  - currentUser là MANAGER
     *  - employeeRepository.findAll trả về list có user & không có user.
     *  Kỳ vọng:
     *  - Chỉ MANAGER được phép
     *  - Mapping đúng, kể cả khi employee.getUser() = null.
     */
    @Test
    void getAllEmployee_shouldReturnList_whenManager() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);
            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));

            // employee có user
            User u1 = new User();
            u1.setId(UUID.randomUUID());
            u1.setUsername("emp1");
            u1.setRole(RoleEnum.GUARD);
            u1.setPhoneNumber("111");
            u1.setEmail("e1@example.com");
            Employee e1 = new Employee();
            e1.setId(UUID.randomUUID());
            e1.setUser(u1);

            // employee không có user (dữ liệu xấu nhưng code vẫn support)
            Employee e2 = new Employee();
            e2.setId(UUID.randomUUID());
            e2.setUser(null);

            when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));

            List<GetAllEmployeeResponse> list = employeeService.getAllEmployee();

            assertEquals(2, list.size());

            GetAllEmployeeResponse r1 = list.get(0);
            assertEquals(e1.getId(), r1.getEmployeeId());
            assertEquals("emp1", r1.getUsername());
            assertEquals(RoleEnum.GUARD, r1.getRole());
            assertEquals("111", r1.getPhone());
            assertEquals("e1@example.com", r1.getEmail());

            GetAllEmployeeResponse r2 = list.get(1);
            assertEquals(e2.getId(), r2.getEmployeeId());
            assertNull(r2.getUsername());
            assertNull(r2.getRole());
            assertNull(r2.getPhone());
            assertNull(r2.getEmail());
        }
    }

    /**
     * 🎯 TC12 – currentUser không phải MANAGER → không được phép xem list employee.
     */
    @Test
    void getAllEmployee_shouldThrow_whenCurrentUserNotManager() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID guardId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardId);

            User guard = new User();
            guard.setId(guardId);
            guard.setRole(RoleEnum.GUARD);
            when(userRepository.findById(guardId)).thenReturn(Optional.of(guard));

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.getAllEmployee()
            );

            assertTrue(ex.getMessage().contains("permission"));
            verify(employeeRepository, never()).findAll();
        }
    }

    // --------------------------------------------------
    // getEmployeeById
    // --------------------------------------------------

    /**
     * 🎯 TC13 – Lấy thông tin employee theo ID thành công.
     */
    @Test
    void getEmployeeById_shouldReturnEmployee_whenExists() {
        UUID empId = UUID.randomUUID();

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUserCode("EMP001");
        u.setDob(LocalDate.of(2000, 1, 1));
        u.setEmail("emp@example.com");
        u.setRole(RoleEnum.GUARD);
        u.setGender(GenderEnum.MALE);
        u.setFullName("Emp Name");
        u.setPhoneNumber("0123");

        Employee e = new Employee();
        e.setId(empId);
        e.setUser(u);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(e));

        GetEmployeeByIdResponse resp = employeeService.getEmployeeById(empId);

        assertEquals(empId, resp.getEmployeeId());
        assertEquals(u.getId(), resp.getUserId());
        assertEquals("EMP001", resp.getUserCode());
        assertEquals(u.getDob(), resp.getDob());
        assertEquals(u.getEmail(), resp.getEmail());
        assertEquals(u.getRole(), resp.getRole());
        assertEquals(u.getGender(), resp.getGender());
        assertEquals(u.getFullName(), resp.getFullName());
        assertEquals(u.getPhoneNumber(), resp.getPhoneNumber());
    }

    /**
     * 🎯 TC14 – getEmployeeById với ID không tồn tại → NotFoundException.
     */
    @Test
    void getEmployeeById_shouldThrow_whenEmployeeNotFound() {
        UUID empId = UUID.randomUUID();
        when(employeeRepository.findById(empId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> employeeService.getEmployeeById(empId)
        );

        assertEquals("Employee not found", ex.getMessage());
    }

    // --------------------------------------------------
    // updateEmployee
    // --------------------------------------------------

    /**
     * 🎯 TC15 – Manager cập nhật employee thành công:
     *  - Manager được phép update bất kỳ employee nào
     *  - Role mới phải là GUARD/CLEANER
     *  - contractEndDate >= hireDate
     */
    @Test
    void updateEmployee_shouldUpdateSuccessfully_whenManager() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            User empUser = new User();
            empUser.setId(UUID.randomUUID());
            empUser.setFullName("Emp Name");
            empUser.setDob(LocalDate.of(2000, 1, 1));
            empUser.setRole(RoleEnum.GUARD);
            empUser.setPhoneNumber("000");

            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);
            employee.setHireDate(LocalDate.of(2024, 1, 1));

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setPhoneNumber("999");
            req.setBirthDate(LocalDate.of(1999, 12, 31));
            req.setRole(RoleEnum.CLEANER);
            req.setContractEndDate(LocalDate.of(2025, 1, 1));

            UpdateEmployeeResponse resp = employeeService.updateEmployee(employee.getId(), req);

            verify(userRepository).findById(managerId);
            verify(employeeRepository).findById(employee.getId());
            verify(userRepository).save(empUser);
            verify(employeeRepository).save(employee);

            assertEquals("999", empUser.getPhoneNumber());
            assertEquals(LocalDate.of(1999, 12, 31), empUser.getDob());
            assertEquals(RoleEnum.CLEANER, empUser.getRole());
            assertEquals(LocalDate.of(2025, 1, 1), employee.getContractEndDate());

            assertEquals(employee.getId(), resp.getEmployeeId());
            assertEquals(empUser.getFullName(), resp.getFullName());
            assertEquals(empUser.getDob(), resp.getBirthDate());
            assertEquals(LocalDate.of(2025, 1, 1), resp.getContractEndDate());
        }
    }

    /**
     * 🎯 TC16 – Owner (chính employee đó) được phép update thông tin của mình.
     */
    @Test
    void updateEmployee_shouldAllowOwnerToUpdateSelf() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID employeeUserId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(employeeUserId);

            User currentUser = new User();
            currentUser.setId(employeeUserId);
            currentUser.setRole(RoleEnum.GUARD);

            User empUser = currentUser; // chính nó
            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);
            employee.setHireDate(LocalDate.of(2023, 1, 1));

            when(userRepository.findById(employeeUserId)).thenReturn(Optional.of(currentUser));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setPhoneNumber("555");
            req.setBirthDate(LocalDate.of(1998, 5, 5));
            req.setRole(RoleEnum.GUARD);
            req.setContractEndDate(LocalDate.of(2024, 1, 1));

            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateEmployeeResponse resp = employeeService.updateEmployee(employee.getId(), req);

            assertEquals("555", empUser.getPhoneNumber());
            assertEquals(LocalDate.of(1998, 5, 5), empUser.getDob());
            assertEquals(RoleEnum.GUARD, empUser.getRole());
            assertEquals(LocalDate.of(2024, 1, 1), employee.getContractEndDate());
            assertEquals(employee.getId(), resp.getEmployeeId());
        }
    }

    /**
     * 🎯 TC17 – User không phải manager, cũng không phải owner → bị chặn.
     */
    @Test
    void updateEmployee_shouldThrow_whenNotManagerAndNotOwner() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID currentId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentId);

            User currentUser = new User();
            currentUser.setId(currentId);
            currentUser.setRole(RoleEnum.GUARD);

            User otherUser = new User();
            otherUser.setId(UUID.randomUUID());
            otherUser.setRole(RoleEnum.GUARD);

            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(otherUser);

            when(userRepository.findById(currentId)).thenReturn(Optional.of(currentUser));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setRole(RoleEnum.GUARD);
            req.setContractEndDate(LocalDate.of(2024, 1, 1));

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.updateEmployee(employee.getId(), req)
            );

            assertTrue(ex.getMessage().contains("not allowed"));
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC18 – Role update không phải GUARD/CLEANER/TECHNICAL:
     *  - currentUser: MANAGER
     *  - request.role = RESIDENT
     *  Kỳ vọng:
     *  - Ném BadHttpRequestException với message:
     *    "You only can update this employee to GUARD, CLEANER or TECHNICAL"
     *  - Không save user/employee.
     */
    @Test
    void updateEmployee_shouldThrow_whenRoleInvalid() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            User empUser = new User();
            empUser.setId(UUID.randomUUID());
            empUser.setRole(RoleEnum.GUARD);

            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setRole(RoleEnum.RESIDENT); // ❌ không hợp lệ

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.updateEmployee(employee.getId(), req)
            );

            assertEquals("You only can update this employee to GUARD, CLEANER or TECHNICAL", ex.getMessage());
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC19 – ContractEndDate < hireDate khi update → reject.
     */
    @Test
    void updateEmployee_shouldThrow_whenContractEndBeforeHireDate() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            User empUser = new User();
            empUser.setId(UUID.randomUUID());
            empUser.setRole(RoleEnum.GUARD);

            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);
            employee.setHireDate(LocalDate.of(2024, 1, 1));

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            UpdateEmployeeRequest req = new UpdateEmployeeRequest();
            req.setRole(RoleEnum.GUARD);
            req.setContractEndDate(LocalDate.of(2023, 12, 31)); // trước hireDate

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.updateEmployee(employee.getId(), req)
            );

            assertTrue(ex.getMessage().contains("Contract end date must be after hire date"));
            verify(userRepository, never()).save(any());
            verify(employeeRepository, never()).save(any());
        }
    }

    // --------------------------------------------------
    // resetPassword
    // --------------------------------------------------

    /**
     * 🎯 TC20 – Manager reset password cho nhân viên khác thành công.
     */
    @Test
    void resetPassword_shouldAllowManagerToResetOthersPassword() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            User empUser = new User();
            empUser.setId(UUID.randomUUID());
            empUser.setPassword("old");
            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("newPass");

            ResetPasswordResponse resp = employeeService.resetPassword(employee.getId(), req);

            verify(passwordEncoder).encode("newPass");
            assertEquals("encodedNew", empUser.getPassword());
            assertEquals(employee.getId(), resp.getEmployeeId());
        }
    }

    /**
     * 🎯 TC21 – Owner được phép tự đổi password của mình.
     */
    @Test
    void resetPassword_shouldAllowOwnerToResetSelfPassword() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID empUserId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(empUserId);

            User currentUser = new User();
            currentUser.setId(empUserId);
            currentUser.setRole(RoleEnum.GUARD);

            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(currentUser);

            when(userRepository.findById(empUserId)).thenReturn(Optional.of(currentUser));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(passwordEncoder.encode("secret123")).thenReturn("enc-secret");

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("secret123");

            ResetPasswordResponse resp = employeeService.resetPassword(employee.getId(), req);

            assertEquals("enc-secret", currentUser.getPassword());
            assertEquals(employee.getId(), resp.getEmployeeId());
        }
    }

    /**
     * 🎯 TC22 – Không phải manager, không phải owner → không được reset password.
     */
    @Test
    void resetPassword_shouldThrow_whenNotManagerAndNotOwner() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID currentId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentId);

            User currentUser = new User();
            currentUser.setId(currentId);
            currentUser.setRole(RoleEnum.GUARD);

            User otherUser = new User();
            otherUser.setId(UUID.randomUUID());
            otherUser.setRole(RoleEnum.GUARD);

            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(otherUser);

            when(userRepository.findById(currentId)).thenReturn(Optional.of(currentUser));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("123456");

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.resetPassword(employee.getId(), req)
            );

            assertTrue(ex.getMessage().contains("not allowed"));
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC23 – Password mới null/blank.
     */
    @Test
    void resetPassword_shouldThrow_whenNewPasswordMissing() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            User empUser = new User();
            empUser.setId(UUID.randomUUID());
            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("  "); // blank

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.resetPassword(employee.getId(), req)
            );

            assertEquals("Password is required", ex.getMessage());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }
    }

    /**
     * 🎯 TC24 – Password mới quá ngắn (< 6 ký tự).
     */
    @Test
    void resetPassword_shouldThrow_whenNewPasswordTooShort() {
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            UUID managerId = UUID.randomUUID();
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            User manager = new User();
            manager.setId(managerId);
            manager.setRole(RoleEnum.MANAGER);

            User empUser = new User();
            empUser.setId(UUID.randomUUID());
            Employee employee = new Employee();
            employee.setId(UUID.randomUUID());
            employee.setUser(empUser);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

            ResetPasswordRequest req = new ResetPasswordRequest();
            req.setNewPassword("123"); // ngắn

            BadHttpRequestException ex = assertThrows(
                    BadHttpRequestException.class,
                    () -> employeeService.resetPassword(employee.getId(), req)
            );

            assertEquals("Password must be at least 6 characters", ex.getMessage());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }
    }
}
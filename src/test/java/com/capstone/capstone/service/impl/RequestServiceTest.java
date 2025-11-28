package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RequestStatusEnum;
import com.capstone.capstone.dto.enums.RequestTypeEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllAnonymousRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllRequestResponse;
import com.capstone.capstone.dto.response.request.GetRequestByIdResponse;
import com.capstone.capstone.dto.response.request.UpdateRequestResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private RequestService requestService;

    // ---------------------------------------------------------
    // createRequest
    // ---------------------------------------------------------

    // ✅ TC1: Tạo request thành công với dữ liệu hợp lệ
    // - Lấy đúng user hiện tại
    // - Tìm đúng semester theo ngày hiện tại
    // - Tìm đúng slot theo user
    // - Lưu request
    // - Mapping response đúng: type, status, content, semesterId, requestId, userId, userName, roomNumber
    @Test
    void createRequest_shouldCreateSuccessfully_whenValid() {
        UUID currentUserId = UUID.randomUUID();

        User user = new User();
        user.setId(currentUserId);
        user.setUsername("duongnt");

        Semester semester = new Semester();
        UUID semesterId = UUID.randomUUID();
        semester.setId(semesterId);
        semester.setName("Fall 2025");

        Room room = new Room();
        room.setRoomNumber("A101");

        Slot slot = new Slot();
        slot.setUser(user);
        slot.setRoom(room);

        CreateRequestRequest req = new CreateRequestRequest();
        req.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        req.setContent("Bóng đèn bị hỏng");

        // mock static AuthenUtil.getCurrentUserId()
        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(semesterRepository.findSemesterByCurrentDate(any(LocalDate.class)))
                    .thenReturn(Optional.of(semester));
            when(slotRepository.findByUser(user)).thenReturn(slot);

            // Khi save, gán id cho request
            when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
                Request r = invocation.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

            // Act
            CreateRequestResponse resp = requestService.createRequest(req);

            // Assert – verify tương tác
            verify(userRepository, times(1)).findById(currentUserId);
            verify(semesterRepository, times(1)).findSemesterByCurrentDate(any(LocalDate.class));
            verify(slotRepository, times(1)).findByUser(user);
            verify(requestRepository, times(1)).save(any(Request.class));

            assertNotNull(resp);
            assertNotNull(resp.getRequestId());
            assertEquals(RequestTypeEnum.TECHNICAL_ISSUE, resp.getRequestType());
            assertEquals(RequestStatusEnum.PENDING, resp.getRequestStatus());
            assertEquals("Bóng đèn bị hỏng", resp.getContent());
            assertEquals(semesterId, resp.getSemesterId());
            assertEquals(currentUserId, resp.getUseId());
            assertEquals("duongnt", resp.getUserName());
            assertNull(resp.getExecuteTime(), "Request mới tạo thì executeTime phải null");
            assertNotNull(resp.getCreateTime());
        }
    }

    // ❌ TC2: createRequest – user hiện tại không tồn tại → NotFoundException
    @Test
    void createRequest_shouldThrowNotFound_whenCurrentUserNotFound() {
        UUID currentUserId = UUID.randomUUID();

        CreateRequestRequest req = new CreateRequestRequest();
        req.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        req.setContent("Test");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> requestService.createRequest(req)
            );

            assertEquals("User not found", ex.getMessage());
            verify(userRepository, times(1)).findById(currentUserId);
            verifyNoInteractions(semesterRepository, slotRepository, requestRepository);
        }
    }

    // ❌ TC3: createRequest – không tìm thấy semester hiện tại → NotFoundException
    @Test
    void createRequest_shouldThrowNotFound_whenSemesterNotFound() {
        UUID currentUserId = UUID.randomUUID();
        User user = new User();
        user.setId(currentUserId);

        CreateRequestRequest req = new CreateRequestRequest();
        req.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        req.setContent("Test");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(semesterRepository.findSemesterByCurrentDate(any(LocalDate.class)))
                    .thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> requestService.createRequest(req)
            );

            assertEquals("Semester not found", ex.getMessage());
            verify(userRepository, times(1)).findById(currentUserId);
            verify(semesterRepository, times(1)).findSemesterByCurrentDate(any(LocalDate.class));
            verifyNoInteractions(slotRepository, requestRepository);
        }
    }

    // ❌ TC4: createRequest – không có slot cho user → NotFoundException
    @Test
    void createRequest_shouldThrowNotFound_whenSlotNotFound() {
        UUID currentUserId = UUID.randomUUID();
        User user = new User();
        user.setId(currentUserId);

        Semester semester = new Semester();
        semester.setId(UUID.randomUUID());

        CreateRequestRequest req = new CreateRequestRequest();
        req.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        req.setContent("Test");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(currentUserId);

            when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
            when(semesterRepository.findSemesterByCurrentDate(any(LocalDate.class)))
                    .thenReturn(Optional.of(semester));
            when(slotRepository.findByUser(user)).thenReturn(null); // ofNullable(null) → throw

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> requestService.createRequest(req)
            );

            assertEquals("Slot not found", ex.getMessage());
            verify(slotRepository, times(1)).findByUser(user);
            verify(requestRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------
    // updateRequest
    // ---------------------------------------------------------

    // ✅ TC5: updateRequest – GUARD cập nhật request → set message nhân viên + status, set executeTime khi ACCEPTED
    @Test
    void updateRequest_shouldUpdateByGuard_whenAccepted() {
        UUID requestId = UUID.randomUUID();
        UUID guardUserId = UUID.randomUUID();

        User guard = new User();
        guard.setId(guardUserId);
        guard.setRole(RoleEnum.GUARD);

        Request existing = new Request();
        existing.setId(requestId);
        existing.setRequestStatus(RequestStatusEnum.PENDING);
        existing.setExecuteTime(null);
        existing.setUser(guard); // không quan trọng lắm chỗ này

        UpdateRequestRequest req = new UpdateRequestRequest();
        req.setRequestStatus(RequestStatusEnum.ACCEPTED);
        req.setResponseMessage("Đã xử lý xong.");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardUserId);

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(existing));
            when(userRepository.findById(guardUserId)).thenReturn(Optional.of(guard));
            when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateRequestResponse resp = requestService.updateRequest(req, requestId);

            // Verify tương tác
            verify(requestRepository, times(1)).findById(requestId);
            verify(userRepository, times(1)).findById(guardUserId);
            verify(requestRepository, times(1)).save(existing);

            assertEquals(requestId, resp.getRequestId());
            assertEquals(existing.getUser().getId(), resp.getUseId());
            assertEquals(RequestStatusEnum.ACCEPTED, resp.getRequestStatus());
            assertEquals("Đã xử lý xong.", resp.getResponseMessageByEmployee());
            assertNull(resp.getResponseMessageByManager());

            assertNotNull(resp.getExecuteTime(), "Khi ACCEPTED thì executeTime phải được set");
        }
    }

    // ✅ TC6: updateRequest – MANAGER cập nhật → set message manager + status, nhưng không AccessDenied
    @Test
    void updateRequest_shouldUpdateByManager() {
        UUID requestId = UUID.randomUUID();
        UUID managerUserId = UUID.randomUUID();

        User manager = new User();
        manager.setId(managerUserId);
        manager.setRole(RoleEnum.MANAGER);

        User resident = new User();
        resident.setId(UUID.randomUUID());

        Request existing = new Request();
        existing.setId(requestId);
        existing.setUser(resident);
        existing.setRequestStatus(RequestStatusEnum.PENDING);

        UpdateRequestRequest req = new UpdateRequestRequest();
        req.setRequestStatus(RequestStatusEnum.REJECTED);
        req.setResponseMessage("Không hợp lệ");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerUserId);

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(existing));
            when(userRepository.findById(managerUserId)).thenReturn(Optional.of(manager));
            when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateRequestResponse resp = requestService.updateRequest(req, requestId);

            verify(requestRepository, times(1)).findById(requestId);
            verify(userRepository, times(1)).findById(managerUserId);
            verify(requestRepository, times(1)).save(existing);

            assertEquals(RequestStatusEnum.REJECTED, resp.getRequestStatus());
            assertEquals("Không hợp lệ", resp.getResponseMessageByManager());
            assertNull(resp.getResponseMessageByEmployee());
            assertNotNull(resp.getExecuteTime(), "Khi REJECTED thì executeTime phải được set");
        }
    }

    // ❌ TC7: updateRequest – RESIDENT cố update → AccessDeniedException, không save
    @Test
    void updateRequest_shouldThrowAccessDenied_whenUserIsResident() {
        UUID requestId = UUID.randomUUID();
        UUID residentId = UUID.randomUUID();

        User resident = new User();
        resident.setId(residentId);
        resident.setRole(RoleEnum.RESIDENT);

        Request existing = new Request();
        existing.setId(requestId);

        UpdateRequestRequest req = new UpdateRequestRequest();
        req.setRequestStatus(RequestStatusEnum.ACCEPTED);
        req.setResponseMessage("Hack nè");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(residentId);

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(existing));
            when(userRepository.findById(residentId)).thenReturn(Optional.of(resident));

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> requestService.updateRequest(req, requestId)
            );

            assertEquals("Access denied", ex.getMessage());
            verify(requestRepository, times(1)).findById(requestId);
            verify(userRepository, times(1)).findById(residentId);
            verify(requestRepository, never()).save(any());
        }
    }

    // ❌ TC8: updateRequest – requestId không tồn tại → NotFoundException
    @Test
    void updateRequest_shouldThrowNotFound_whenRequestNotFound() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UpdateRequestRequest req = new UpdateRequestRequest();
        req.setRequestStatus(RequestStatusEnum.ACCEPTED);
        req.setResponseMessage("Test");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> requestService.updateRequest(req, requestId)
            );

            assertEquals("Request not found", ex.getMessage());
            verify(requestRepository, times(1)).findById(requestId);
            verify(userRepository, never()).findById(any());
            verify(requestRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------
    // getRequestById
    // ---------------------------------------------------------

    // ✅ TC9: getRequestById – map đúng dữ liệu
    @Test
    void getRequestById_shouldReturnMappedResponse_whenRequestExists() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("resident01");

        Semester semester = new Semester();
        semester.setName("Spring 2026");

        Room room = new Room();
        room.setRoomNumber("B202");

        Slot slot = new Slot();
        slot.setUser(user);
        slot.setRoom(room);

        Request req = new Request();
        req.setId(requestId);
        req.setUser(user);
        req.setSemester(semester);
        req.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        req.setContent("Lỗi wifi");
        req.setCreateTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        req.setExecuteTime(LocalDateTime.of(2025, 1, 2, 12, 0));
        req.setResponseByEmployeeMessage("Đã xử lý");
        req.setResponseByManagerMessage("OK");
        req.setRequestStatus(RequestStatusEnum.ACCEPTED);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(req));
        when(slotRepository.findByUser(user)).thenReturn(slot);

        GetRequestByIdResponse resp = requestService.getRequestById(requestId);

        verify(requestRepository, times(1)).findById(requestId);
        verify(slotRepository, times(1)).findByUser(user);

        assertEquals(requestId, resp.getRequestId());
        assertEquals(RequestTypeEnum.TECHNICAL_ISSUE, resp.getRequestType());
        assertEquals("Lỗi wifi", resp.getContent());
        assertEquals(req.getCreateTime(), resp.getCreateTime());
        assertEquals(req.getExecuteTime(), resp.getExecuteTime());
        assertEquals("Đã xử lý", resp.getResponseMessageByEmployee());
        assertEquals("OK", resp.getResponseMessageByManager());
        assertEquals("Spring 2026", resp.getSemesterName());
        assertEquals(RequestStatusEnum.ACCEPTED, resp.getResponseStatus());
        assertEquals(userId, resp.getUserId());
        assertEquals("B202", resp.getRoomName());
        assertEquals(RequestStatusEnum.ACCEPTED, resp.getStatus());
    }

    // ❌ TC10: getRequestById – request không tồn tại → NotFoundException
    @Test
    void getRequestById_shouldThrowNotFound_whenRequestNotFound() {
        UUID requestId = UUID.randomUUID();
        when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> requestService.getRequestById(requestId)
        );

        assertEquals("Request not found", ex.getMessage());
        verify(requestRepository, times(1)).findById(requestId);
        verifyNoInteractions(slotRepository);
    }

    // ---------------------------------------------------------
    // getAllRequest
    // ---------------------------------------------------------

    // ✅ TC11: getAllRequest – role MANAGER → gọi findAll, filter bỏ ANONYMOUS, map đúng
    @Test
    void getAllRequest_shouldReturnAllForManager_andFilterAnonymous() {
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        User resident = new User();
        resident.setId(UUID.randomUUID());
        resident.setUsername("res01");

        Semester semester = new Semester();
        semester.setName("Fall 2025");

        Request normalReq = new Request();
        normalReq.setId(UUID.randomUUID());
        normalReq.setUser(resident);
        normalReq.setSemester(semester);
        normalReq.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        normalReq.setCreateTime(LocalDateTime.now());
        normalReq.setRequestStatus(RequestStatusEnum.PENDING);
        normalReq.setRoomNumber("C303");

        Request anonymousReq = new Request();
        anonymousReq.setId(UUID.randomUUID());
        anonymousReq.setUser(resident);
        anonymousReq.setSemester(semester);
        anonymousReq.setRequestType(RequestTypeEnum.ANONYMOUS);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(requestRepository.findAll()).thenReturn(List.of(normalReq, anonymousReq));

            List<GetAllRequestResponse> result = requestService.getAllRequest();

            verify(userRepository, times(1)).findById(managerId);
            verify(requestRepository, times(1)).findAll();

            assertEquals(1, result.size(), "Anonymous request phải bị filter bỏ");
            GetAllRequestResponse r = result.get(0);
            assertEquals(normalReq.getId(), r.getRequestId());
            assertEquals(resident.getId(), r.getResidentId());
            assertEquals("res01", r.getResidentName());
            assertEquals(RequestTypeEnum.TECHNICAL_ISSUE, r.getRequestType());
            assertEquals("C303", r.getRoomName());
            assertEquals("Fall 2025", r.getSemesterName());
        }
    }

    // ✅ TC12: getAllRequest – role RESIDENT → chỉ thấy request của mình
    @Test
    void getAllRequest_shouldReturnResidentOwnRequests_whenRoleResident() {
        UUID residentId = UUID.randomUUID();
        User resident = new User();
        resident.setId(residentId);
        resident.setRole(RoleEnum.RESIDENT);
        resident.setUsername("res01");

        Semester semester = new Semester();
        semester.setName("Fall 2025");

        Request ownReq = new Request();
        ownReq.setId(UUID.randomUUID());
        ownReq.setUser(resident);
        ownReq.setSemester(semester);
        ownReq.setRequestType(RequestTypeEnum.TECHNICAL_ISSUE);
        ownReq.setRequestStatus(RequestStatusEnum.PENDING);
        ownReq.setRoomNumber("D404");

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(residentId);

            when(userRepository.findById(residentId)).thenReturn(Optional.of(resident));
            when(requestRepository.findRequestByUser(resident))
                    .thenReturn(List.of(ownReq));

            List<GetAllRequestResponse> result = requestService.getAllRequest();

            verify(requestRepository, times(1)).findRequestByUser(resident);
            assertEquals(1, result.size());
            assertEquals("D404", result.get(0).getRoomName());
        }
    }

    // ❌ TC13: getAllRequest – role không được phép → AccessDeniedException
    @Test
    void getAllRequest_shouldThrowAccessDenied_whenRoleUnknown() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        // giả sử có role mới chưa được handle
        user.setRole(null);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> requestService.getAllRequest()
            );

            assertEquals("Access denied", ex.getMessage());
            verify(requestRepository, never()).findAll();
        }
    }

    // ---------------------------------------------------------
    // getAllAnonymousRequest
    // ---------------------------------------------------------

    // ✅ TC14: MANAGER xem được danh sách anonymous, mapping đúng dữ liệu
    @Test
    void getAllAnonymousRequest_shouldReturnMappedList_whenUserIsManager() {
        // Arrange
        UUID managerId = UUID.randomUUID();
        User manager = new User();
        manager.setId(managerId);
        manager.setRole(RoleEnum.MANAGER);

        Semester semester = new Semester();
        semester.setName("Spring 2025");

        Request r1 = new Request();
        r1.setId(UUID.randomUUID());
        r1.setSemester(semester);
        r1.setContent("Góp ý 1");
        r1.setCreateTime(LocalDateTime.now());
        r1.setRequestType(RequestTypeEnum.ANONYMOUS);

        Request r2 = new Request();
        r2.setId(UUID.randomUUID());
        r2.setSemester(semester);
        r2.setContent("Góp ý 2");
        r2.setCreateTime(LocalDateTime.now());
        r2.setRequestType(RequestTypeEnum.ANONYMOUS);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(managerId);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(requestRepository.findRequestByRequestType(RequestTypeEnum.ANONYMOUS))
                    .thenReturn(List.of(r1, r2));

            // Act
            List<GetAllAnonymousRequestResponse> result = requestService.getAllAnonymousRequest();

            // Assert
            verify(userRepository, times(1)).findById(managerId);
            verify(requestRepository, times(1))
                    .findRequestByRequestType(RequestTypeEnum.ANONYMOUS);

            assertEquals(2, result.size());

            GetAllAnonymousRequestResponse first = result.get(0);
            assertEquals(r1.getId(), first.getRequestId());
            assertEquals("Góp ý 1", first.getContent());
            assertEquals("Spring 2025", first.getSemesterName());
            assertNotNull(first.getCreateTime());
        }
    }

    // ✅ TC15: ADMIN cũng xem được danh sách anonymous
    @Test
    void getAllAnonymousRequest_shouldReturnMappedList_whenUserIsAdmin() {
        UUID adminId = UUID.randomUUID();
        User admin = new User();
        admin.setId(adminId);
        admin.setRole(RoleEnum.ADMIN);

        Semester semester = new Semester();
        semester.setName("Fall 2025");

        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setSemester(semester);
        r.setContent("Góp ý ADMIN");
        r.setCreateTime(LocalDateTime.now());
        r.setRequestType(RequestTypeEnum.ANONYMOUS);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(adminId);

            when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(requestRepository.findRequestByRequestType(RequestTypeEnum.ANONYMOUS))
                    .thenReturn(List.of(r));

            List<GetAllAnonymousRequestResponse> result = requestService.getAllAnonymousRequest();

            verify(requestRepository, times(1))
                    .findRequestByRequestType(RequestTypeEnum.ANONYMOUS);
            assertEquals(1, result.size());
            assertEquals("Góp ý ADMIN", result.get(0).getContent());
            assertEquals("Fall 2025", result.get(0).getSemesterName());
        }
    }

    // ✅ TC16: User không phải MANAGER/ADMIN (ví dụ GUARD) → AccessDeniedException
    @Test
    void getAllAnonymousRequest_shouldThrowAccessDenied_whenUserIsNotManagerOrAdmin() {
        UUID guardId = UUID.randomUUID();
        User guard = new User();
        guard.setId(guardId);
        guard.setRole(RoleEnum.GUARD);

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(guardId);

            when(userRepository.findById(guardId)).thenReturn(Optional.of(guard));

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> requestService.getAllAnonymousRequest()
            );

            assertTrue(ex.getMessage().toLowerCase().contains("access denied"));
            verify(requestRepository, never())
                    .findRequestByRequestType(any());
        }
    }

    // ✅ TC17: Current user không tồn tại → NotFoundException
    @Test
    void getAllAnonymousRequest_shouldThrowNotFound_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        try (MockedStatic<AuthenUtil> mockedStatic = mockStatic(AuthenUtil.class)) {
            mockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(
                    NotFoundException.class,
                    () -> requestService.getAllAnonymousRequest()
            );

            assertEquals("User not found", ex.getMessage());
            verify(requestRepository, never())
                    .findRequestByRequestType(any());
        }
    }
}
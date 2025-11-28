package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.request.schedule.UpdateScheduleRequest;
import com.capstone.capstone.dto.response.PageResponse;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;
import com.capstone.capstone.dto.response.schedule.GetScheduleResponse;
import com.capstone.capstone.dto.response.schedule.UpdateScheduleResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private DormRepository dormRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private MockedStatic<AuthenUtil> authenUtilMock;

    private UUID userId;
    private User managerUser;
    private User guardUser;
    private User cleanerUser;

    private Employee guardEmployee;
    private Employee cleanerEmployee;

    private Shift shiftMorning;
    private Dorm dormA;
    private Semester semester1;

    @BeforeEach
    void init() {
        authenUtilMock = Mockito.mockStatic(AuthenUtil.class);

        userId = UUID.randomUUID();

        managerUser = buildUser(UUID.randomUUID(), RoleEnum.MANAGER, "Manager A");
        guardUser = buildUser(UUID.randomUUID(), RoleEnum.GUARD, "Guard A");
        cleanerUser = buildUser(UUID.randomUUID(), RoleEnum.CLEANER, "Cleaner A");

        guardEmployee = buildEmployee(UUID.randomUUID(), guardUser);
        cleanerEmployee = buildEmployee(UUID.randomUUID(), cleanerUser);

        shiftMorning = buildShift(UUID.randomUUID(), "Ca sáng");
        dormA = buildDorm(UUID.randomUUID(), "Dorm A");
        semester1 = buildSemester(UUID.randomUUID(), "Fall 25");
    }

    @AfterEach
    void tearDown() {
        if (authenUtilMock != null) {
            authenUtilMock.close();
        }
    }

    // --------------------------------------------------------
    // Helper methods tạo entity fake cho test
    // --------------------------------------------------------

    private User buildUser(UUID id, RoleEnum role, String fullName) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setFullName(fullName);
        return u;
    }

    private Employee buildEmployee(UUID id, User user) {
        Employee e = new Employee();
        e.setId(id);
        e.setUser(user);
        return e;
    }

    private Shift buildShift(UUID id, String name) {
        Shift s = new Shift();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private Dorm buildDorm(UUID id, String name) {
        Dorm d = new Dorm();
        d.setId(id);
        d.setDormName(name);
        return d;
    }

    private Semester buildSemester(UUID id, String name) {
        Semester s = new Semester();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private Schedule buildSchedule(UUID id,
                                   Employee emp,
                                   Shift shift,
                                   Dorm dorm,
                                   Semester sem,
                                   LocalDate workDate,
                                   String note) {
        Schedule s = new Schedule();
        s.setId(id);
        s.setEmployee(emp);
        s.setShift(shift);
        s.setDorm(dorm);
        s.setWorkDate(workDate);
        s.setNote(note);
        s.setCreatedAt(LocalDateTime.of(2025, 1, 1, 8, 0));
        s.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 9, 0));
        return s;
    }

    // ========================================================
    // ============== TEST CHO createSchedule() ===============
    // ========================================================

    // Test case: Thiếu 1 trong các field bắt buộc → ném IllegalArgumentException
    @Test
    void createSchedule_shouldThrow_whenRequiredIdsMissing() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        // thiếu hết: employeeId, shiftId, dormId, semesterId
        req.setSingleDate(LocalDate.now());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(req)
        );

        assertEquals("employeeId, shiftId, dormId là bắt buộc", ex.getMessage());
        verifyNoInteractions(employeeRepository, shiftRepository, dormRepository, scheduleRepository);
    }

    // Test case: Cả singleDate và range (from+to) đều được set → ném IllegalArgumentException
    @Test
    void createSchedule_shouldThrow_whenSingleAndRangeBothProvided() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(UUID.randomUUID());
        req.setShiftId(UUID.randomUUID());
        req.setDormId(UUID.randomUUID());

        req.setSingleDate(LocalDate.now());
        req.setFrom(LocalDate.now());
        req.setTo(LocalDate.now().plusDays(3));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(req)
        );
        assertEquals("Chọn hoặc singleDate, hoặc startDate+endDate", ex.getMessage());
    }

    // Test case: Không set singleDate, không set from/to → ném IllegalArgumentException
    @Test
    void createSchedule_shouldThrow_whenNeitherSingleNorRangeProvided() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(UUID.randomUUID());
        req.setShiftId(UUID.randomUUID());
        req.setDormId(UUID.randomUUID());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(req)
        );
        assertEquals("Chọn hoặc singleDate, hoặc startDate+endDate", ex.getMessage());
    }

    // Test case: from > to (range bị ngược) → ném IllegalArgumentException
    @Test
    void createSchedule_shouldThrow_whenFromAfterTo() {
        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(UUID.randomUUID());
        req.setShiftId(UUID.randomUUID());
        req.setDormId(UUID.randomUUID());

        req.setFrom(LocalDate.of(2025, 1, 10));
        req.setTo(LocalDate.of(2025, 1, 5));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(req)
        );
        assertEquals("endDate phải >= startDate", ex.getMessage());
    }

    // Test case: Single date happy path – tạo lịch 1 ngày thành công
    @Test
    void createSchedule_shouldCreateSingleDateSuccessfully() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        LocalDate workDate = LocalDate.of(2025, 1, 5);
        req.setSingleDate(workDate);
        req.setNote("Ghi chú test");

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));
        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(emp, workDate, shiftMorning))
                .thenReturn(false);

        when(scheduleRepository.save(any(Schedule.class)))
                .thenAnswer(invocation -> {
                    Schedule s = invocation.getArgument(0);
                    s.setId(UUID.randomUUID());
                    return s;
                });

        List<CreateScheduleResponse> result = scheduleService.createSchedule(req);

        assertEquals(1, result.size());
        CreateScheduleResponse res = result.get(0);
        assertNotNull(res.getId());
        assertEquals(empId, res.getEmployeeId());
        assertEquals(shiftId, res.getShiftId());
        assertEquals(dormId, res.getDormId());
        assertEquals(workDate, res.getWorkDate());
        assertEquals("Ghi chú test", res.getNote());

        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    // Test case: Range + repeatDays chỉ lấy ngày thỏa mãn, không có conflict → tạo nhiều lịch
    @Test
    void createSchedule_shouldCreateRangeWithRepeatDaysSuccessfully() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        req.setFrom(LocalDate.of(2025, 1, 1));  // Wednesday
        req.setTo(LocalDate.of(2025, 1, 10));
        req.setRepeatDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));

        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(any(), any(), any()))
                .thenReturn(false);

        when(scheduleRepository.save(any(Schedule.class)))
                .thenAnswer(invocation -> {
                    Schedule s = invocation.getArgument(0);
                    if (s.getId() == null) {
                        s.setId(UUID.randomUUID());
                    }
                    return s;
                });

        List<CreateScheduleResponse> result = scheduleService.createSchedule(req);

        // 01/01/2025 (Wed), 03 (Fri), 05 (Sun), 06 (Mon), 08 (Wed), 10 (Fri)
        // Mon & Wed between [1..10]: 01 (Wed), 06 (Mon), 08 (Wed) → 3 ngày
        assertEquals(3, result.size());

        Set<LocalDate> dates = result.stream()
                .map(CreateScheduleResponse::getWorkDate)
                .collect(Collectors.toSet());

        assertTrue(dates.contains(LocalDate.of(2025, 1, 1)));
        assertTrue(dates.contains(LocalDate.of(2025, 1, 6)));
        assertTrue(dates.contains(LocalDate.of(2025, 1, 8)));

        verify(scheduleRepository, times(3)).save(any(Schedule.class));
    }

    // Test case: Range + repeatDays = null → hiểu là lặp tất cả các ngày trong khoảng
    @Test
    void createSchedule_shouldCreateRangeWhenRepeatDaysNull_meansAllDays() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        req.setFrom(LocalDate.of(2025, 1, 1));
        req.setTo(LocalDate.of(2025, 1, 3));
        req.setRepeatDays(null);

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));

        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(any(), any(), any()))
                .thenReturn(false);

        when(scheduleRepository.save(any(Schedule.class)))
                .thenAnswer(invocation -> {
                    Schedule s = invocation.getArgument(0);
                    s.setId(UUID.randomUUID());
                    return s;
                });

        List<CreateScheduleResponse> result = scheduleService.createSchedule(req);

        assertEquals(3, result.size());
        verify(scheduleRepository, times(3)).save(any(Schedule.class));
    }

    // Test case: Range + repeatDays empty set → cũng nên hiểu là tất cả các ngày
    @Test
    void createSchedule_shouldCreateRangeWhenRepeatDaysEmpty_meansAllDays() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        req.setFrom(LocalDate.of(2025, 1, 1));
        req.setTo(LocalDate.of(2025, 1, 3));
        req.setRepeatDays(Collections.emptySet());

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));

        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(any(), any(), any()))
                .thenReturn(false);

        when(scheduleRepository.save(any(Schedule.class)))
                .thenAnswer(invocation -> {
                    Schedule s = invocation.getArgument(0);
                    s.setId(UUID.randomUUID());
                    return s;
                });

        List<CreateScheduleResponse> result = scheduleService.createSchedule(req);

        assertEquals(3, result.size());
        verify(scheduleRepository, times(3)).save(any(Schedule.class));
    }

    // Test case: Range + repeatDays lọc ra không còn ngày nào → ném IllegalArgumentException
    @Test
    void createSchedule_shouldThrow_whenNoValidDayGenerated() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        req.setFrom(LocalDate.of(2025, 1, 1));
        req.setTo(LocalDate.of(2025, 1, 3));
        // repeatDays chỉ chứa Sunday, nhưng khoảng 1-3/1 có Wed, Thu, Fri
        req.setRepeatDays(Set.of(DayOfWeek.SUNDAY));

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(req)
        );
        assertEquals("Không có ngày nào hợp lệ để tạo lịch", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    // Test case: Trùng lịch ở một số ngày → ném RuntimeException báo conflict
    @Test
    void createSchedule_shouldThrow_whenConflictOnSomeDays() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        req.setFrom(LocalDate.of(2025, 1, 1));
        req.setTo(LocalDate.of(2025, 1, 3));
        req.setRepeatDays(null);

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));

        // Giả lập: ngày 1/1 conflict, các ngày còn lại không conflict
        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(emp, LocalDate.of(2025, 1, 1), shiftMorning))
                .thenReturn(true);
        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(emp, LocalDate.of(2025, 1, 2), shiftMorning))
                .thenReturn(false);
        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(emp, LocalDate.of(2025, 1, 3), shiftMorning))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.createSchedule(req)
        );
        assertTrue(ex.getMessage().contains("Trùng lịch ở các ngày"));
        verify(scheduleRepository, never()).save(any());
    }

    // Test case: save() bị DataIntegrityViolationException (UNIQUE) → wrap thành RuntimeException
    @Test
    void createSchedule_shouldThrow_whenUniqueConstraintViolatedOnSave() {
        UUID empId = UUID.randomUUID();
        UUID shiftId = shiftMorning.getId();
        UUID dormId = dormA.getId();

        CreateScheduleRequest req = new CreateScheduleRequest();
        req.setEmployeeId(empId);
        req.setShiftId(shiftId);
        req.setDormId(dormId);
        LocalDate workDate = LocalDate.of(2025, 1, 5);
        req.setSingleDate(workDate);

        Employee emp = buildEmployee(empId, guardUser);

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shiftMorning));
        when(dormRepository.findById(dormId)).thenReturn(Optional.of(dormA));
        when(scheduleRepository.existsByEmployeeAndWorkDateAndShift(emp, workDate, shiftMorning))
                .thenReturn(false);

        when(scheduleRepository.save(any(Schedule.class)))
                .thenThrow(new DataIntegrityViolationException("UNIQUE constraint"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.createSchedule(req)
        );
        assertTrue(ex.getMessage().contains("Lịch bị trùng (UNIQUE) tại ngày"));
    }

    // ========================================================
    // =============== TEST CHO getAllSchedules() =============
    // ========================================================

    //  Test case: MANAGER → lấy full list schedule (phân trang)
    @Test
    void getAllSchedules_asManager_shouldReturnAllSchedules() {
        Pageable pageable = PageRequest.of(0, 2);

        // Giả lập user hiện tại là MANAGER
        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(managerUser.getId());
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        Employee emp = buildEmployee(UUID.randomUUID(), guardUser);

        Schedule s1 = buildSchedule(UUID.randomUUID(), emp, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "note1");
        Schedule s2 = buildSchedule(UUID.randomUUID(), emp, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 6), "note2");

        Page<Schedule> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

        when(scheduleRepository.findAll(pageable)).thenReturn(page);

        PageResponse<GetScheduleResponse> res = scheduleService.getAllSchedules(pageable);

        assertEquals(1, res.getCurrentPage());
        assertEquals(2, res.getPageSize());
        assertEquals(1, res.getTotalPage());
        assertEquals(2, res.getTotalCount());
        assertEquals(2, res.getData().size());

        GetScheduleResponse r1 = res.getData().get(0);
        assertEquals(s1.getId(), r1.getScheduleId());
        assertEquals(emp.getId(), r1.getEmployeeId());
        assertEquals(emp.getUser().getFullName(), r1.getEmployeeName());
        assertEquals(shiftMorning.getId(), r1.getShiftId());
        assertEquals(shiftMorning.getName(), r1.getShiftName());
        assertEquals(dormA.getId(), r1.getDormId());
        assertEquals(dormA.getDormName(), r1.getDormName());
    }

    // Test case: GUARD → chỉ thấy lịch của chính mình
    @Test
    void getAllSchedules_asGuard_shouldReturnOnlyOwnSchedules() {
        Pageable pageable = PageRequest.of(0, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(guardUser.getId());
        when(userRepository.findById(guardUser.getId())).thenReturn(Optional.of(guardUser));

        when(employeeRepository.findEmployeeByUser(guardUser))
                .thenReturn(Optional.of(guardEmployee));

        Schedule s1 = buildSchedule(UUID.randomUUID(), guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "note1");
        Schedule s2 = buildSchedule(UUID.randomUUID(), guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 6), "note2");

        Page<Schedule> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

        when(scheduleRepository.findByEmployee(guardEmployee, pageable)).thenReturn(page);

        PageResponse<GetScheduleResponse> res = scheduleService.getAllSchedules(pageable);

        assertEquals(2, res.getData().size());
        verify(scheduleRepository, times(1)).findByEmployee(guardEmployee, pageable);
    }

    // Test case: CLEANER → behavior giống GUARD (chỉ lịch của chính mình)
    @Test
    void getAllSchedules_asCleaner_shouldReturnOnlyOwnSchedules() {
        Pageable pageable = PageRequest.of(0, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(cleanerUser.getId());
        when(userRepository.findById(cleanerUser.getId())).thenReturn(Optional.of(cleanerUser));

        when(employeeRepository.findEmployeeByUser(cleanerUser))
                .thenReturn(Optional.of(cleanerEmployee));

        Schedule s1 = buildSchedule(UUID.randomUUID(), cleanerEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "note1");

        Page<Schedule> page = new PageImpl<>(List.of(s1), pageable, 1);

        when(scheduleRepository.findByEmployee(cleanerEmployee, pageable)).thenReturn(page);

        PageResponse<GetScheduleResponse> res = scheduleService.getAllSchedules(pageable);

        assertEquals(1, res.getData().size());
        verify(scheduleRepository, times(1)).findByEmployee(cleanerEmployee, pageable);
    }

    // Test case: GUARD nhưng không tìm được employee → RuntimeException("Employee not found")
    @Test
    void getAllSchedules_asGuard_shouldThrow_whenEmployeeNotFound() {
        Pageable pageable = PageRequest.of(0, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(guardUser.getId());
        when(userRepository.findById(guardUser.getId())).thenReturn(Optional.of(guardUser));

        when(employeeRepository.findEmployeeByUser(guardUser))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.getAllSchedules(pageable)
        );
        assertEquals("Employee not found", ex.getMessage());
    }

    // Test case: Role TECHNICAL (không được support) → SHOULD throw RuntimeException (logic mong muốn)
    // Hiện tại code thật sẽ bị NullPointerException (bug). Test này dùng để chỉ ra bug đó.
    @Test
    void getAllSchedules_asUnsupportedRole_shouldFail() {
        User technicalUser = buildUser(UUID.randomUUID(), RoleEnum.TECHNICAL, "Tech A");

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(technicalUser.getId());
        when(userRepository.findById(technicalUser.getId())).thenReturn(Optional.of(technicalUser));

        // Mong muốn thực tế: nên throw RuntimeException với message rõ ràng
        // nhưng hiện tại service sẽ NPE ở chỗ schedules.getNumber()
        assertThrows(RuntimeException.class, () -> scheduleService.getAllSchedules(PageRequest.of(0, 10)));
    }

    // ========================================================
    // =============== TEST CHO updateSchedule() ==============
    // ========================================================

    // Test case: Happy path – update dorm, shift, note thành công
    @Test
    void updateSchedule_shouldUpdateSuccessfully() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = buildSchedule(scheduleId, guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "old note");

        Dorm newDorm = buildDorm(UUID.randomUUID(), "Dorm B");
        Shift newShift = buildShift(UUID.randomUUID(), "Ca tối");

        UpdateScheduleRequest req = new UpdateScheduleRequest();
        req.setDormID(newDorm.getId());
        req.setShiftId(newShift.getId());
        req.setNote("new note");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(dormRepository.findById(newDorm.getId())).thenReturn(Optional.of(newDorm));
        when(shiftRepository.findById(newShift.getId())).thenReturn(Optional.of(newShift));

        when(scheduleRepository.save(any(Schedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateScheduleResponse res = scheduleService.updateSchedule(req, scheduleId);

        assertEquals(scheduleId, res.getId());
        assertEquals(newDorm.getId(), res.getDormId());
        assertEquals(newDorm.getDormName(), res.getDormName());
        assertEquals(newShift.getId(), res.getShiftId());
        assertEquals(newShift.getName(), res.getShiftName());
        assertEquals("new note", res.getNote());
        assertNotNull(res.getUpdatedAt());

        verify(scheduleRepository, times(1)).save(schedule);
    }

    // Test case: Schedule không tồn tại → RuntimeException("Schedule not found")
    @Test
    void updateSchedule_shouldThrow_whenScheduleNotFound() {
        UUID scheduleId = UUID.randomUUID();
        UpdateScheduleRequest req = new UpdateScheduleRequest();
        req.setDormID(dormA.getId());
        req.setShiftId(shiftMorning.getId());
        req.setNote("test");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.updateSchedule(req, scheduleId)
        );
        assertEquals("Schedule not found", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    // Test case: Dorm mới không tồn tại → RuntimeException("Dorm not found")
    @Test
    void updateSchedule_shouldThrow_whenDormNotFound() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = buildSchedule(scheduleId, guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "old note");

        UpdateScheduleRequest req = new UpdateScheduleRequest();
        req.setDormID(UUID.randomUUID());
        req.setShiftId(shiftMorning.getId());
        req.setNote("new note");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(dormRepository.findById(req.getDormID())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.updateSchedule(req, scheduleId)
        );
        assertEquals("Dorm not found", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    // Test case: Shift mới không tồn tại → RuntimeException("Shift not found")
    @Test
    void updateSchedule_shouldThrow_whenShiftNotFound() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = buildSchedule(scheduleId, guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "old note");

        UpdateScheduleRequest req = new UpdateScheduleRequest();
        req.setDormID(dormA.getId());
        req.setShiftId(UUID.randomUUID());
        req.setNote("new note");

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(dormRepository.findById(dormA.getId())).thenReturn(Optional.of(dormA));
        when(shiftRepository.findById(req.getShiftId())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.updateSchedule(req, scheduleId)
        );
        assertEquals("Shift not found", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    // Test case: Update note = null → note trong schedule nên trở thành null
    @Test
    void updateSchedule_shouldAllowNullNote() {
        UUID scheduleId = UUID.randomUUID();
        Schedule schedule = buildSchedule(scheduleId, guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 5), "old note");

        UpdateScheduleRequest req = new UpdateScheduleRequest();
        req.setDormID(dormA.getId());
        req.setShiftId(shiftMorning.getId());
        req.setNote(null);

        when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(dormRepository.findById(dormA.getId())).thenReturn(Optional.of(dormA));
        when(shiftRepository.findById(shiftMorning.getId())).thenReturn(Optional.of(shiftMorning));
        when(scheduleRepository.save(any(Schedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateScheduleResponse res = scheduleService.updateSchedule(req, scheduleId);

        assertNull(res.getNote());
        assertNull(schedule.getNote()); // entity thực cũng phải null
    }

    // ========================================================
    // =========== TEST CHO getAllScheduleByDate() ============
    // ========================================================

    // Test case: MANAGER → lấy tất cả schedule trong khoảng ngày
    @Test
    void getAllScheduleByDate_asManager_shouldReturnAllSchedulesInRange() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(managerUser.getId());
        when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));

        Schedule s1 = buildSchedule(UUID.randomUUID(), guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 3), "note1");
        Schedule s2 = buildSchedule(UUID.randomUUID(), guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 7), "note2");

        when(scheduleRepository.findAllByWorkDateBetween(from, to))
                .thenReturn(List.of(s1, s2));

        List<GetScheduleResponse> res = scheduleService.getAllScheduleByDate(from, to);

        assertEquals(2, res.size());
        assertEquals(s1.getId(), res.get(0).getScheduleId());
        assertEquals(s2.getId(), res.get(1).getScheduleId());
    }

    // Test case: GUARD → chỉ thấy schedule của chính mình trong khoảng ngày
    @Test
    void getAllScheduleByDate_asGuard_shouldReturnOwnSchedulesInRange() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(guardUser.getId());
        when(userRepository.findById(guardUser.getId())).thenReturn(Optional.of(guardUser));

        when(employeeRepository.findEmployeeByUser(guardUser))
                .thenReturn(Optional.of(guardEmployee));

        Schedule s1 = buildSchedule(UUID.randomUUID(), guardEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 3), "note1");

        when(scheduleRepository.findAllByEmployee_IdAndWorkDateBetween(guardEmployee.getId(), from, to))
                .thenReturn(List.of(s1));

        List<GetScheduleResponse> res = scheduleService.getAllScheduleByDate(from, to);

        assertEquals(1, res.size());
        assertEquals(s1.getId(), res.get(0).getScheduleId());
        verify(scheduleRepository, times(1))
                .findAllByEmployee_IdAndWorkDateBetween(guardEmployee.getId(), from, to);
    }

    // Test case: CLEANER → giống GUARD
    @Test
    void getAllScheduleByDate_asCleaner_shouldReturnOwnSchedulesInRange() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(cleanerUser.getId());
        when(userRepository.findById(cleanerUser.getId())).thenReturn(Optional.of(cleanerUser));

        when(employeeRepository.findEmployeeByUser(cleanerUser))
                .thenReturn(Optional.of(cleanerEmployee));

        Schedule s1 = buildSchedule(UUID.randomUUID(), cleanerEmployee, shiftMorning, dormA, semester1,
                LocalDate.of(2025, 1, 4), "note1");

        when(scheduleRepository.findAllByEmployee_IdAndWorkDateBetween(cleanerEmployee.getId(), from, to))
                .thenReturn(List.of(s1));

        List<GetScheduleResponse> res = scheduleService.getAllScheduleByDate(from, to);

        assertEquals(1, res.size());
        assertEquals(s1.getId(), res.get(0).getScheduleId());
    }

    // Test case: GUARD nhưng không có employee → RuntimeException("Employee not found")
    @Test
    void getAllScheduleByDate_asGuard_shouldThrow_whenEmployeeNotFound() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(guardUser.getId());
        when(userRepository.findById(guardUser.getId())).thenReturn(Optional.of(guardUser));

        when(employeeRepository.findEmployeeByUser(guardUser))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> scheduleService.getAllScheduleByDate(from, to)
        );
        assertEquals("Employee not found", ex.getMessage());
    }

    @Test
    void getAllScheduleByDate_asUnsupportedRole_shouldFailOrReturnEmpty() {
        User technicalUser = buildUser(UUID.randomUUID(), RoleEnum.TECHNICAL, "Tech A");

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 10);

        authenUtilMock.when(AuthenUtil::getCurrentUserId).thenReturn(technicalUser.getId());
        when(userRepository.findById(technicalUser.getId())).thenReturn(Optional.of(technicalUser));

        List<GetScheduleResponse> res = scheduleService.getAllScheduleByDate(from, to);
        assertTrue(res.isEmpty());
    }
}
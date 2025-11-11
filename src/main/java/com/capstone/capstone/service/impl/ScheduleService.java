package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.response.PageResponse;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;
import com.capstone.capstone.dto.response.schedule.GetScheduleResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.service.interfaces.IScheduleService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ScheduleService implements IScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final DormRepository dormRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;


    @Override
    public List<CreateScheduleResponse> createSchedule(CreateScheduleRequest req) {
        // 1) Validate bắt buộc
        if (req.getEmployeeId() == null || req.getShiftId() == null ||
                req.getDormId() == null || req.getSemesterId() == null) {
            throw new IllegalArgumentException("employeeId, shiftId, dormId, semesterId là bắt buộc");
        }

        boolean singleDay = req.getSingleDate() != null;
        boolean range = req.getFrom() != null && req.getTo() != null;

        if (!(singleDay ^ range)) { // chỉ được chọn 1 kiểu
            throw new IllegalArgumentException("Chọn hoặc singleDate, hoặc startDate+endDate");
        }
        if (range && req.getFrom().isAfter(req.getTo())) { // <-- sửa ở đây
            throw new IllegalArgumentException("endDate phải >= startDate");
        }

        // 2) Load entities
        Employee emp = employeeRepository.findById(req.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Shift shift = shiftRepository.findById(req.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        Dorm dorm = dormRepository.findById(req.getDormId())
                .orElseThrow(() -> new RuntimeException("Dorm not found"));
        Semester sem = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new RuntimeException("Semester not found"));

        // 3) Build danh sách ngày cần insert
        List<LocalDate> days = new ArrayList<>();
        if (singleDay) {
            days.add(req.getSingleDate());
        } else {
            Set<DayOfWeek> repeat = req.getRepeatDays();
            LocalDate d = req.getFrom();
            while (!d.isAfter(req.getTo())) {
                if (repeat == null || repeat.isEmpty() || repeat.contains(d.getDayOfWeek())) {
                    days.add(d);
                }
                d = d.plusDays(1);
            }
        }
        if (days.isEmpty()) {
            throw new IllegalArgumentException("Không có ngày nào hợp lệ để tạo lịch");
        }

        // 4) Kiểm tra trùng (báo sớm)
        List<LocalDate> conflicts = new ArrayList<>();
        for (LocalDate day : days) {
            boolean exists = scheduleRepository.existsByEmployeeAndWorkDateAndShift(emp, day, shift);
            if (exists) conflicts.add(day);
        }
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Trùng lịch ở các ngày: " + conflicts);
        }

        // 5) Lưu & trả về
        List<CreateScheduleResponse> out = new ArrayList<>();
        for (LocalDate day : days) {
            Schedule s = new Schedule();
            s.setEmployee(emp);
            s.setShift(shift);
            s.setDorm(dorm);
            s.setSemester(sem);
            s.setWorkDate(day);
            s.setNote(req.getNote());

            try {
                Schedule saved = scheduleRepository.save(s);
                out.add(toResponse(saved));
            } catch (DataIntegrityViolationException e) {
                // Phòng race condition UNIQUE
                throw new RuntimeException("Lịch bị trùng (UNIQUE) tại ngày: " + day, e);
            }
        }
        return out;
    }

    private CreateScheduleResponse toResponse(Schedule s) {
        CreateScheduleResponse r = new CreateScheduleResponse();
        r.setId(s.getId());
        r.setEmployeeId(s.getEmployee().getId());
        r.setShiftId(s.getShift().getId());
        r.setDormId(s.getDorm().getId());
        r.setSemesterId(s.getSemester().getId());
        r.setWorkDate(s.getWorkDate());
        r.setNote(s.getNote());
        r.setEmployeeName(s.getEmployee().getUser().getFullName());
        r.setShiftName(s.getShift().getName());
        r.setDormName(s.getDorm().getDormName());

        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        return r;
    }


    @Override
    public PageResponse<GetScheduleResponse> getAllSchedules(Pageable pageable) {
        UUID currentUserId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
        Page<Schedule> schedules = null;
        if (user.getRole().equals(RoleEnum.MANAGER)) {
            schedules = scheduleRepository.findAll(pageable);
        } else if(user.getRole().equals(RoleEnum.GUARD) || user.getRole().equals(RoleEnum.CLEANER)) {
            Employee emp = employeeRepository.findEmployeeByUser(user).orElseThrow(() -> new RuntimeException("Employee not found"));
            schedules = scheduleRepository.findByEmployee(emp, pageable);
        }
        PageResponse<GetScheduleResponse> response = new PageResponse<>();
        response.setCurrentPage(schedules.getNumber() + 1);
        response.setPageSize(schedules.getSize());
        response.setTotalPage(schedules.getTotalPages());
        response.setTotalCount(schedules.getNumberOfElements());
        response.setData(schedules.stream().map(s -> {
            GetScheduleResponse resp = new GetScheduleResponse();
            resp.setScheduleId(s.getId());
            resp.setEmployeeId(s.getEmployee().getId());
            resp.setEmployeeName(s.getEmployee().getUser().getFullName());
            resp.setShiftId(s.getShift().getId());
            resp.setShiftName(s.getShift().getName());
            resp.setSemesterId(s.getSemester().getId());
            resp.setSemesterName(s.getSemester().getName());
            resp.setDormId(s.getDorm().getId());
            resp.setDormName(s.getDorm().getDormName());
            resp.setCreatedAt(s.getCreatedAt());
            resp.setUpdatedAt(null);
            resp.setNote(s.getNote());
            return resp;
        }).collect(Collectors.toList()));
        return response;
    }
}

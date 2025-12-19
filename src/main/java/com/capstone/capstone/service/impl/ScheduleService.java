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
import com.capstone.capstone.service.interfaces.IScheduleService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ScheduleService implements IScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final DormRepository dormRepository;
    private final UserRepository userRepository;


    @Override
    public List<CreateScheduleResponse> createSchedule(CreateScheduleRequest req) {
        // 1) Validate bắt buộc
        if (req.getEmployeeId() == null || req.getShiftId() == null ||
                req.getDormId() == null) {
            throw new IllegalArgumentException("employeeId, shiftId, dormId là bắt buộc");
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
            resp.setDormId(s.getDorm().getId());
            resp.setDormName(s.getDorm().getDormName());
            resp.setCreatedAt(s.getCreatedAt());
            resp.setUpdatedAt(null);
            resp.setNote(s.getNote());
            return resp;
        }).collect(Collectors.toList()));
        return response;
    }

    @Override
    public UpdateScheduleResponse updateSchedule(UpdateScheduleRequest request, UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
        Dorm newDorm = dormRepository.findById(request.getDormID()).orElseThrow(() -> new RuntimeException("Dorm not found"));
        Shift shift = shiftRepository.findById(request.getShiftId()).orElseThrow(() -> new RuntimeException("Shift not found"));
        schedule.setDorm(newDorm);
        schedule.setNote(request.getNote());
        schedule.setShift(shift);
        schedule.setUpdatedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);
        UpdateScheduleResponse r = new UpdateScheduleResponse();
        r.setId(schedule.getId());
        r.setEmployeeId(schedule.getEmployee().getId());
        r.setEmployeeName(schedule.getEmployee().getUser().getFullName());
        r.setNote(schedule.getNote());
        r.setShiftId(schedule.getShift().getId());
        r.setShiftName(schedule.getShift().getName());
        r.setDormId(schedule.getDorm().getId());
        r.setDormName(schedule.getDorm().getDormName());
        r.setWorkDate(schedule.getWorkDate());
        r.setCreatedAt(schedule.getCreatedAt());
        r.setUpdatedAt(schedule.getUpdatedAt());
        return r;
    }

    @Override
    public List<GetScheduleResponse> getAllScheduleByDate(LocalDate from, LocalDate to) {
        UUID currentUserId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Schedule> schedules = new ArrayList<>();
        if (user.getRole().equals(RoleEnum.MANAGER)) {
            schedules = scheduleRepository.findAllByWorkDateBetween(from, to);
        } else if(user.getRole().equals(RoleEnum.GUARD) || user.getRole().equals(RoleEnum.CLEANER)) {
            Employee emp = employeeRepository.findEmployeeByUser(user).orElseThrow(() -> new RuntimeException("Employee not found"));
            schedules = scheduleRepository.findAllByEmployee_IdAndWorkDateBetween(emp.getId(), from, to);
        }
        List<GetScheduleResponse> response = new ArrayList<>();
        for(Schedule s : schedules) {
            GetScheduleResponse resp = new GetScheduleResponse();
            resp.setScheduleId(s.getId());
            resp.setEmployeeId(s.getEmployee().getId());
            resp.setEmployeeName(s.getEmployee().getUser().getFullName());
            resp.setShiftId(s.getShift().getId());
            resp.setShiftName(s.getShift().getName());
            resp.setDormId(s.getDorm().getId());
            resp.setDormName(s.getDorm().getDormName());
            resp.setCreatedAt(s.getCreatedAt());
            resp.setUpdatedAt(s.getUpdatedAt());
            resp.setNote(s.getNote());
            resp.setWorkDate(s.getWorkDate());
            response.add(resp);

        }
        return response;
    }

    @Override
    public Void deleteSchedule(UUID scheduleId) {
        UUID currentUserId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole().equals(RoleEnum.MANAGER)) {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new RuntimeException("Schedule not found"));
            scheduleRepository.delete(schedule);
        } else {
            throw new RuntimeException("User not allowed to delete schedule");
        }
        return null;
    }
}

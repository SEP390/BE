package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.schedule.CreateScheduleRequest;
import com.capstone.capstone.dto.response.schedule.CreateScheduleResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.service.interfaces.IScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class ScheduleService implements IScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final DormRepository dormRepository;
    private final SemesterRepository semesterRepository;


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
}

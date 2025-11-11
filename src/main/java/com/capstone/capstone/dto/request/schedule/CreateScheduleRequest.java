package com.capstone.capstone.dto.request.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateScheduleRequest {
    // BẮT BUỘC
    private UUID employeeId;
    private UUID shiftId;
    private UUID dormId;

    // TUỲ CHỌN
    private UUID semesterId;

    private String note;

    // --- tạo 1 ngày ---
    private LocalDate singleDate;

    // --- tạo nhiều ngày ---
    private LocalDate from;      // inclusive
    private LocalDate to;        // inclusive
    private Set<DayOfWeek> repeatDays;

}

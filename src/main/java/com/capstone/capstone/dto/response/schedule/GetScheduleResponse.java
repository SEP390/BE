package com.capstone.capstone.dto.response.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetScheduleResponse {
    private UUID scheduleId;
    private UUID employeeId;
    private String employeeName;
    private UUID shiftId;
    private String shiftName;
    private UUID semesterId;
    private String semesterName;
    private UUID dormId;
    private String dormName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate workDate;
    private String note;
}

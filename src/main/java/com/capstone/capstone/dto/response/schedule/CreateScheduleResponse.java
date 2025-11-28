package com.capstone.capstone.dto.response.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateScheduleResponse {
    UUID id;
    UUID employeeId;
    String employeeName;
    UUID shiftId;
    String shiftName;
    UUID dormId;
    String dormName;
    LocalDate workDate;
    String note;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

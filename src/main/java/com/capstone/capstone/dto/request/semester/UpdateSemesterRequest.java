package com.capstone.capstone.dto.request.semester;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateSemesterRequest {
    @NotNull(message = "SEMESTER_NAME_NULL")
    private String name;
    @NotNull(message = "SEMESTER_START_DATE_NULL")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @NotNull(message = "SEMESTER_END_DATE_NULL")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

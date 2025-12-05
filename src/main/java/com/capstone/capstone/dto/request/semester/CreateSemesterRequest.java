package com.capstone.capstone.dto.request.semester;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSemesterRequest {
    @NotBlank(message = "SEMESTER_NAME_NULL")
    private String name;
    @NotNull(message = "SEMESTER_START_DATE_NULL")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @NotNull(message = "SEMESTER_END_DATE_NULL")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}

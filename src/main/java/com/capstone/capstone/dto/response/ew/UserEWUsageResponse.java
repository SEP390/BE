package com.capstone.capstone.dto.response.ew;

import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserEWUsageResponse {
    private Integer electric;
    private Integer water;
    private Boolean paid;
    private LocalDate startDate;
    private LocalDate endDate;
    private SemesterResponse semester;
}

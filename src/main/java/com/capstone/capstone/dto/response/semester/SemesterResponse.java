package com.capstone.capstone.dto.response.semester;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SemesterResponse {
    private UUID id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}

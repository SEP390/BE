package com.capstone.capstone.dto.response.semester;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterResponse {
    private UUID id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}

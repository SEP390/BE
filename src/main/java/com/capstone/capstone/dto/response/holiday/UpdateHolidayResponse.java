package com.capstone.capstone.dto.response.holiday;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateHolidayResponse {
    private String holidayName;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID semesterId;
}

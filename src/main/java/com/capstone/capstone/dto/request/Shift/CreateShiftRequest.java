package com.capstone.capstone.dto.request.Shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateShiftRequest {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}

package com.capstone.capstone.dto.request.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateShiftRequest {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}

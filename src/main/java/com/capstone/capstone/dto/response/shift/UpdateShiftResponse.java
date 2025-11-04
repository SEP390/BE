package com.capstone.capstone.dto.response.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateShiftResponse {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}

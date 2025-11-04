package com.capstone.capstone.dto.response.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateShiftResponse {
    private UUID id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}

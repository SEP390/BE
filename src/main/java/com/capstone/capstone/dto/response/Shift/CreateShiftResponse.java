package com.capstone.capstone.dto.response.Shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateShiftResponse {
    private UUID id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

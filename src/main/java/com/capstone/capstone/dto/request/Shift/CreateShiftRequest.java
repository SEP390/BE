package com.capstone.capstone.dto.request.Shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateShiftRequest {
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

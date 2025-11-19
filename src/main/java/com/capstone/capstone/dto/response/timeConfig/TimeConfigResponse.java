package com.capstone.capstone.dto.response.timeConfig;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TimeConfigResponse {
    private LocalDate startBookingDate;
    private LocalDate endBookingDate;
    private LocalDate startExtendDate;
    private LocalDate endExtendDate;
    private LocalDateTime createTime;
}

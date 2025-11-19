package com.capstone.capstone.dto.request.timeConfig;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTimeConfigRequest {
    private LocalDate startBookingDate;
    private LocalDate endBookingDate;
    private LocalDate startExtendDate;
    private LocalDate endExtendDate;
}

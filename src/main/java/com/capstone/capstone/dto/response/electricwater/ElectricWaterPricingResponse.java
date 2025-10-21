package com.capstone.capstone.dto.response.electricwater;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ElectricWaterPricingResponse {
    private UUID id;
    private Long electricPrice;
    private Long waterPrice;
    private LocalDateTime startDate;
}

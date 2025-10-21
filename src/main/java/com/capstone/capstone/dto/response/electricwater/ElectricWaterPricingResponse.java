package com.capstone.capstone.dto.response.electricwater;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ElectricWaterPricingResponse {
    private Long electricPrice;
    private Long waterPrice;
    private LocalDate startDate;
}

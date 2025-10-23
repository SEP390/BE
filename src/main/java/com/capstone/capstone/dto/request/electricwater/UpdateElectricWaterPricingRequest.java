package com.capstone.capstone.dto.request.electricwater;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateElectricWaterPricingRequest {
    @NotNull(message = "PRICING_ID_NULL")
    private UUID id;
    @NotNull(message = "ELECTRIC_PRICING_NULL")
    @Min(value = 0, message = "ELECTRIC_PRICING_MIN")
    private Long electricPrice;
    @NotNull(message = "WATER_PRICING_NULL")
    @Min(value = 0, message = "WATER_PRICING_MIN")
    private Long waterPrice;
}

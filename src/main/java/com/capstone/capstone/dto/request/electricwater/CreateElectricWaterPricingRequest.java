package com.capstone.capstone.dto.request.electricwater;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateElectricWaterPricingRequest {
    @NotNull(message = "ELECTRIC_PRICING_NULL")
    @Min(value = 0, message = "ELECTRIC_PRICING_MIN")
    private Long electricPrice;
    @NotNull(message = "WATER_PRICING_NULL")
    @Min(value = 0, message = "WATER_PRICING_MIN")
    private Long waterPrice;
}

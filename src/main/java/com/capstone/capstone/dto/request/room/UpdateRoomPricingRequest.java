package com.capstone.capstone.dto.request.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoomPricingRequest {
    @NotNull(message = "PRICING_NULL")
    @Min(value = 0, message = "PRICING_MIN")
    @Max(value = 1000000000, message = "PRICING_MAX")
    private Long price;
}

package com.capstone.capstone.dto.request.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomPricingRequest {
    @NotNull(message = "TOTAL_SLOT_NULL")
    @Min(value = 1, message = "TOTAL_SLOT_MIN")
    @Max(value = 255, message = "TOTAL_SLOT_MAX")
    private Integer totalSlot;
    @NotNull(message = "PRICING_NULL")
    @Min(value = 0, message = "PRICING_MIN")
    @Max(value = 1000000000, message = "PRICING_MAX")
    private Long price;
}

package com.capstone.capstone.dto.response.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomPricingRequest {
    @NotNull
    @Min(1)
    @Max(255)
    private int totalSlot;
    @NotNull
    @Min(0)
    @Max(1000000000)
    private long price;
}

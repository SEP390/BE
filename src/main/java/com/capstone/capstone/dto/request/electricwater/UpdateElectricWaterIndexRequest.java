package com.capstone.capstone.dto.request.electricwater;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateElectricWaterIndexRequest {
    @NotNull(message = "ID_NULL")
    private UUID id;
    @NotNull(message = "ELECTRIC_INDEX_NULL")
    @Min(value = 0, message = "NEGATIVE_ELECTRIC_INDEX")
    private Integer electricIndex;
    @NotNull(message = "WATER_INDEX_NULL")
    @Min(value = 0, message = "NEGATIVE_WATER_INDEX")
    private Integer waterIndex;
}

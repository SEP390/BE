package com.capstone.capstone.dto.request.electricwater;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateElectricWaterIndexRequest {
    @NotNull
    private UUID id;
    @NotNull
    @Min(0)
    private Integer electricIndex;
    @Min(0)
    @NotNull
    private Integer waterIndex;
}

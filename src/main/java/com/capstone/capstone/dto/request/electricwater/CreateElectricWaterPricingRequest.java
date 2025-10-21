package com.capstone.capstone.dto.request.electricwater;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateElectricWaterPricingRequest {
    @NotNull
    @Min(0)
    private Integer electricIndex;
    @NotNull
    @Min(0)
    private Integer waterIndex;
}

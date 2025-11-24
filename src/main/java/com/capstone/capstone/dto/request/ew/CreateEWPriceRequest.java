package com.capstone.capstone.dto.request.ew;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateEWPriceRequest {
    @Min(value = 0, message = "ELECTRIC_PRICE_MIN")
    private Long electricPrice;
    @Min(value = 0, message = "WATER_PRICE_MIN")
    private Long waterPrice;
    @Min(value = 0, message = "MAX_ELECTRIC_INDEX_INDEX_MIN")
    private Long maxElectricIndex;
    @Min(value = 0, message = "MAX_WATER_INDEX_MIN")
    private Long maxWaterIndex;
}

package com.capstone.capstone.dto.request.ew;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateEWPriceRequest {
    @Min(value = 0, message = "ELECTRIC_PRICE_MIN")
    @Max(value = 999999999, message = "ELECTRIC_PRICE_MAX")
    private Long electricPrice;
    @Min(value = 0, message = "WATER_PRICE_MIN")
    @Max(value = 999999999, message = "WATER_PRICE_MAX")
    private Long waterPrice;
    @Min(value = 0, message = "MAX_ELECTRIC_INDEX_MIN")
    @Max(value = 999999999, message = "MAX_ELECTRIC_INDEX_MAX")
    private Integer maxElectricIndex;
    @Min(value = 0, message = "MAX_WATER_INDEX_MIN")
    @Max(value = 999999999, message = "MAX_WATER_INDEX_MAX")
    private Integer maxWaterIndex;
}

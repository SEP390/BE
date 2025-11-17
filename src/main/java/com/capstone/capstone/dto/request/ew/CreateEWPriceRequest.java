package com.capstone.capstone.dto.request.ew;

import lombok.Data;

@Data
public class CreateEWPriceRequest {
    private Long electricPrice;
    private Long waterPrice;
    private Long maxElectricIndex;
    private Long maxWaterIndex;
}

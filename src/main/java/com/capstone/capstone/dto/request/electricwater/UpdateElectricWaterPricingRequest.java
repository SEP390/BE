package com.capstone.capstone.dto.request.electricwater;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateElectricWaterPricingRequest {
    private UUID id;
    private Integer electricIndex;
    private Integer waterIndex;
}

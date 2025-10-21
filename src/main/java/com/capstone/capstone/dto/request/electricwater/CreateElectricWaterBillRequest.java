package com.capstone.capstone.dto.request.electricwater;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateElectricWaterBillRequest {
    @NotNull(message = "INDEX_ID_NULL")
    private UUID indexId;
}

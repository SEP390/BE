package com.capstone.capstone.dto.request.electricwater;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateElectricWaterIndexRequest {
    private UUID roomId;
    private Integer electricIndex;
    private Integer waterIndex;
}

package com.capstone.capstone.dto.request.electricwater;

import lombok.Data;

import java.util.UUID;

@Data
public class ElectricWaterBillRequest {
    private UUID roomId;
    private long price;
    private int kw;
    private int m3;
}

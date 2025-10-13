package com.capstone.capstone.dto.response.electricwater;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ElectricWaterBillResponse {
    private long price;
    private LocalDateTime createDate;
}

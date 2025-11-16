package com.capstone.capstone.dto.response.ew;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EWPriceResponse {
    private Long electricPrice;
    private Long waterPrice;
    private Long maxElectricIndex;
    private Long maxWaterIndex;
    private LocalDateTime createTime;
}

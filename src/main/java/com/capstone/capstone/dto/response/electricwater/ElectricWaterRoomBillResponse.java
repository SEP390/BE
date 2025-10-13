package com.capstone.capstone.dto.response.electricwater;

import lombok.Data;

import java.util.UUID;

@Data
public class ElectricWaterRoomBillResponse {
    private UUID id;
    private Long price;
    private Integer kw;
    private Integer m3;
    private SemesterDto semester;

    @Data
    public static class SemesterDto {
        private String name;
        private UUID id;
    }
}

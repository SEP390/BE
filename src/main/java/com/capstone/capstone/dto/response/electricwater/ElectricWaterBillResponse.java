package com.capstone.capstone.dto.response.electricwater;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ElectricWaterBillResponse {
    private UUID id;
    private long price;
    private LocalDateTime createDate;
    private ElectricWaterRoomBillDto roomBill;
    private PaymentStatus status;

    @Data
    public static class ElectricWaterRoomBillDto {
        private Integer kw;
        private Integer m3;
        private SemesterDto semester;
    }

    @Data
    public static class SemesterDto {
        private UUID id;
        private String semesterName;
    }
}

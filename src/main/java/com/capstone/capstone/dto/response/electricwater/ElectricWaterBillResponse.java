package com.capstone.capstone.dto.response.electricwater;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ElectricWaterBillResponse {
    private UUID id;
    private Long price;
    private Long totalPrice;
    private Integer userCount;
    private LocalDateTime createDate;
    private ElectricWaterIndexResponse index;
    private PaymentStatus status;
}

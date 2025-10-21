package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.electricwater.ElectricWaterBillResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {
    private UUID id;
    private Long price;
    private PaymentStatus status;
    private String note;
    private LocalDateTime createDate;
    private ElectricWaterBillResponse electricWaterBill;
    private SlotHistoryResponse slotHistory;
}

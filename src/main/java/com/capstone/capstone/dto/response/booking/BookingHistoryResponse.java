package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingHistoryResponse {
    private UUID id;
    private SlotHistoryResponse slotHistory;
    private Long price;
    private LocalDateTime createDate;
    private PaymentStatus status;
}

package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingHistoryResponse {
    private UUID semesterId;
    private String semesterName;
    private UUID slotId;
    private String slotName;
    private String roomNumber;
    private UUID roomId;
    private String dormName;
    private int floor;
    private UUID dormId;
    private LocalDateTime createDate;
    private PaymentStatus status;
}

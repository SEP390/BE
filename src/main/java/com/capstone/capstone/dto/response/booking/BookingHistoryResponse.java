package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.payment.PaymentResponse;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingHistoryResponse {
    private UUID id;
    private SemesterResponse semester;
    private PaymentResponse payment;
    private UUID slotId;
    private String slotName;
    private String roomNumber;
    private String dormName;
    private Long price;
}

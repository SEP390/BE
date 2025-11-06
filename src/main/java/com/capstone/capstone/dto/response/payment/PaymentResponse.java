package com.capstone.capstone.dto.response.payment;

import com.capstone.capstone.dto.enums.PaymentStatus;
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
}

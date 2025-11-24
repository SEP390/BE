package com.capstone.capstone.dto.response.invoice;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private PaymentStatus status;
    private InvoiceResponse invoice;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    private Long price;
}

package com.capstone.capstone.dto.response.invoice;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InvoiceResponse {
    private UUID id;
    private Long price;
    private String reason;
    private InvoiceType type;
    private LocalDateTime createTime;
    private PaymentStatus status;

    // special case
    private SlotInvoiceResponse slotInvoice;
}

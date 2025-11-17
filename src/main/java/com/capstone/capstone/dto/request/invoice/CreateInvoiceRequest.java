package com.capstone.capstone.dto.request.invoice;

import com.capstone.capstone.dto.enums.InvoiceType;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateInvoiceRequest {
    private UUID userId;
    private String reason;
    private Long price;
    private InvoiceType type;
}

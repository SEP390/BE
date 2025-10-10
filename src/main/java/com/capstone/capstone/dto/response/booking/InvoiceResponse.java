package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.InvoiceStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class InvoiceResponse {
    private UUID id;
    private long price;
    private InvoiceStatus status;
    private String note;
    private LocalDateTime createDate;
}

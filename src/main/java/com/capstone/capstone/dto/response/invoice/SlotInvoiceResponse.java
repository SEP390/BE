package com.capstone.capstone.dto.response.invoice;

import lombok.Data;

@Data
public class SlotInvoiceResponse {
    private String semesterName;
    private String dormName;
    private String roomNumber;
    private String slotName;
    private Long price;
}

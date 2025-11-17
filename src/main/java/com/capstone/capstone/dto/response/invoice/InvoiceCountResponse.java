package com.capstone.capstone.dto.response.invoice;

import lombok.Data;

@Data
public class InvoiceCountResponse {
    private Long totalCount;
    private Long totalSuccess;
    private Long totalPending;
}

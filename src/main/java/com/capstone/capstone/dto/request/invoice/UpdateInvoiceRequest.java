package com.capstone.capstone.dto.request.invoice;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Data;

@Data
public class UpdateInvoiceRequest {
    private PaymentStatus status;
}

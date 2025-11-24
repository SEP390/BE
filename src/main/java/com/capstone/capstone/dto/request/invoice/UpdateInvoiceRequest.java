package com.capstone.capstone.dto.request.invoice;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import lombok.Data;

@Data
public class UpdateInvoiceRequest {
    private VNPayStatus status;
}

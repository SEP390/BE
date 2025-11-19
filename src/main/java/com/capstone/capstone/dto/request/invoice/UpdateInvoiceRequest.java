package com.capstone.capstone.dto.request.invoice;

import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateInvoiceRequest {
    private VNPayStatus status;
}

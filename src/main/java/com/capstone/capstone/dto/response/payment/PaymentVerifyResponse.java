package com.capstone.capstone.dto.response.payment;

import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Payment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentVerifyResponse {
    private Payment payment;
    private Boolean update;
    private VNPayStatus status;
}

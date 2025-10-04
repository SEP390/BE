package com.capstone.capstone.dto.response.vnpay;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VNPayRequest {
    private String paymentUrl;
    private String createDate;
}

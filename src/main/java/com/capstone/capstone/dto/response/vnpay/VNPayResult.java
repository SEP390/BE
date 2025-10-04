package com.capstone.capstone.dto.response.vnpay;

import lombok.Data;

import java.util.UUID;

@Data
public class VNPayResult {
    private UUID id;
    private VNPayStatus status;
}

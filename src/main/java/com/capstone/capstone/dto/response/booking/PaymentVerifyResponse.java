package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentVerifyResponse {
    private String dormName;
    private String roomNumber;
    private int floor;
    private String slotName;
    private long price;
    private VNPayStatus status;
}

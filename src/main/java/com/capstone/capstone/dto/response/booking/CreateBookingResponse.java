package com.capstone.capstone.dto.response.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CreateBookingResponse {
    private String paymentUrl;
}

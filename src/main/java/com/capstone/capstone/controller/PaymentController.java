package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.payment.PaymentVerifyResponse;
import com.capstone.capstone.service.impl.ElectricWaterService;
import com.capstone.capstone.service.impl.PaymentService;
import com.capstone.capstone.service.impl.RoomSlotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final ElectricWaterService electricWaterService;
    private final RoomSlotService roomSlotService;

    @GetMapping("/api/payment/verify")
    public BaseResponse<?> verify(HttpServletRequest request) {
        PaymentVerifyResponse verifyResponse = paymentService.verify(request);
        if (verifyResponse.getUpdate()) {
            if (verifyResponse.getPayment().getType() == PaymentType.ELECTRIC_WATER){
                electricWaterService.onPayment(verifyResponse.getPayment(), verifyResponse.getStatus());
            }
            if (verifyResponse.getPayment().getType() == PaymentType.BOOKING) {
                roomSlotService.onPayment(verifyResponse.getPayment(), verifyResponse.getStatus());
            }
        }
        return new BaseResponse<>(paymentService.toResponse(verifyResponse.getPayment()));
    }

    @GetMapping("/api/payment/history")
    public BaseResponse<?> history(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault Pageable pageable) {
        return new BaseResponse<>(paymentService.history(status, pageable));
    }
}

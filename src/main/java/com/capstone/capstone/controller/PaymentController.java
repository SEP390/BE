package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.PaymentType;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.payment.PaymentVerifyResponse;
import com.capstone.capstone.service.impl.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentSlotService paymentSlotService;
    private final PaymentElectricWaterService paymentElectricWaterService;

    @GetMapping("/api/payment/verify")
    public BaseResponse<?> verify(HttpServletRequest request) {
        PaymentVerifyResponse verifyResponse = paymentService.verify(request);
        if (verifyResponse.getUpdate()) {
            if (verifyResponse.getPayment().getType() == PaymentType.ELECTRIC_WATER){
                paymentElectricWaterService.onPayment(verifyResponse.getPayment());
            }
            if (verifyResponse.getPayment().getType() == PaymentType.BOOKING) {
                paymentSlotService.onPayment(verifyResponse.getPayment());
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

    @GetMapping("/api/payment/{id}/url")
    public BaseResponse<String> getUrl(@PathVariable UUID id) {
        return new BaseResponse<>(paymentService.createPaymentUrl(paymentService.getById(id)));
    }

    @GetMapping("/api/payment/booking/pending")
    public BaseResponse<String> getPendingBooking() {
        return new BaseResponse<>(paymentSlotService.getPendingPaymentUrl());
    }
}

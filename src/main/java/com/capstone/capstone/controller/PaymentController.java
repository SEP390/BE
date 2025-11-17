package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.service.impl.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/api/payment")
    public BaseResponse<InvoiceResponse> handle(HttpServletRequest request) {
        return new BaseResponse<>(paymentService.handle(request));
    }

    @GetMapping("/api/payment/pending-booking")
    public BaseResponse<String> getPendingBookingUrl() {
        return new BaseResponse<>(paymentService.getPendingBookingUrl());
    }

    @GetMapping("/api/payment/{id}")
    public BaseResponse<String> getInvoicePaymentUrl(@PathVariable UUID id) {
        return new BaseResponse<>(paymentService.getInvoicePaymentUrl(id));
    }
}

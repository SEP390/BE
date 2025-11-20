package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.service.impl.PaymentChangeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentChangeService paymentChangeService;

    @PostMapping("/api/payment")
    public BaseResponse<InvoiceResponse> handle(HttpServletRequest request) {
        return new BaseResponse<>(paymentChangeService.handle(request));
    }
}

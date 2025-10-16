package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/api/payment/verify")
    public BaseResponse<?> verify(HttpServletRequest request) {
        return new BaseResponse<>(paymentService.verify(request));
    }

    @GetMapping("/api/payment/history")
    public BaseResponse<?> history(@RequestParam(required = false) List<PaymentStatus> status, Pageable pageable) {
        return new BaseResponse<>(paymentService.history(status, pageable));
    }
}

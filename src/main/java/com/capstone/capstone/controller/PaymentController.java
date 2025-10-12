package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.booking.PaymentResponse;
import com.capstone.capstone.dto.response.booking.PaymentVerifyResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.service.impl.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/api/payment/verify-slot")
    public BaseResponse<PaymentVerifyResponse> verify(HttpServletRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return new BaseResponse<>(200, "success", paymentService.verifyForSlot(request, user));
    }

    @GetMapping("/api/payment/history")
    public BaseResponse<Page<PaymentResponse>> history(@RequestParam(required = false) Integer page) {
        return new BaseResponse<>(200, "success", paymentService.history(page));
    }
}

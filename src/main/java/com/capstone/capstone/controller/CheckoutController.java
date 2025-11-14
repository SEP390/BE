package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.service.impl.CheckoutService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping("/api/checkout")
    public BaseResponse<String> checkout(@RequestParam UUID userId) {
        checkoutService.checkout(userId);
        return new BaseResponse<>("SUCCESS");
    }
}

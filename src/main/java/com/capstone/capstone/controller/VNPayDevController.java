package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.service.impl.InvoiceService;
import com.capstone.capstone.service.impl.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Only for dev/testing
 */
@RestController
@Profile("dev")
@AllArgsConstructor
public class VNPayDevController {
    private final PaymentService paymentService;
    @PostMapping("/api/vnpay-dev")
    public BaseResponse<?> update(@RequestParam UUID id, @RequestParam VNPayStatus status) {
        VNPayResult res = new VNPayResult();
        res.setId(id);
        res.setStatus(status);
        paymentService.handle(res);
        return new BaseResponse<>("OK");
    }
}

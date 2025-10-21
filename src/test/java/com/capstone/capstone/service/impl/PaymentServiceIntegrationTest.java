package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("dev")
class PaymentServiceIntegrationTest {
    UUID paymentId = UUID.fromString("");
    @Autowired
    PaymentService paymentService;
}
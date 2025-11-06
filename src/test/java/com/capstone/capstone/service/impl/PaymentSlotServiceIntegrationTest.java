package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("dev")
public class PaymentSlotServiceIntegrationTest {
    @Autowired
    private PaymentSlotService paymentSlotService;

    @Test
    public void onPayment() {
        UUID paymentId = UUID.fromString("");
        Payment payment = new Payment();
        payment.setId(paymentId);
        paymentSlotService.onPayment(payment);
    }
}

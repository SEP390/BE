package com.capstone.capstone.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;

@Slf4j
class VNPayServiceTest {
    private VNPayService service;

    @BeforeEach
    public void setup() {
        service = new VNPayService();
        ReflectionTestUtils.setField(service, "HASH_SECRET", "YFRG1AKBDYW9CHRMWM50DO1X2PA5RG6E");
        ReflectionTestUtils.setField(service, "TMNCODE", "LLO1RUC8");
    }
    @Test
    void queryPaymentResult() throws IOException {
        String date = "20251004011946";
        UUID id = UUID.fromString("959b265a-efe8-48fd-a538-a4e8516b87bb");
        log.info("queryPaymentResult: {}", service.queryPaymentResult(id, date));
    }

    @Test
    void createPayment() {
        UUID id = UUID.randomUUID();
        log.info("Create UUID: {}", id);
        log.info("Create Payment: {}", service.createPaymentUrl(id, 100000));
    }
}
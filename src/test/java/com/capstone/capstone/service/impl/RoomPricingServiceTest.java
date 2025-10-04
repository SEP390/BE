package com.capstone.capstone.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class RoomPricingServiceTest {
    @Autowired
    private RoomPricingService roomPricingService;

    @Test
    void getAllRoomPricing() {
        roomPricingService.getAllRoomPricing().forEach(roomPricing -> {
           log.info("Room pricing: {}", roomPricing);
        });
    }
}
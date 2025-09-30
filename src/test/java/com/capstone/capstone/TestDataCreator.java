package com.capstone.capstone;

import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.repository.RoomPricingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
public class TestDataCreator {
    @Autowired
    private RoomPricingRepository roomPricingRepository;

    @Test
    public void generateRoomPricing() {
        List<RoomPricing> roomPricing = List.of(
                RoomPricing.builder().price(1200000).totalSlot(2).build(),
                RoomPricing.builder().price(1000000).totalSlot(4).build(),
                RoomPricing.builder().price(800000).totalSlot(6).build()
        );
        roomPricingRepository.saveAll(roomPricing);
    }
}

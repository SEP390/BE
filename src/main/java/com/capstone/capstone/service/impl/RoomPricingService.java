package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.repository.RoomPricingRepository;
import com.capstone.capstone.service.interfaces.IRoomPricingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RoomPricingService implements IRoomPricingService {
    private final RoomPricingRepository roomPricingRepository;

    public List<RoomPricingResponse> getAllRoomPricing() {
        return roomPricingRepository.findAll().stream().map(pricing -> RoomPricingResponse.builder()
                .price(pricing.getPrice())
                .id(pricing.getId())
                .totalSlot(pricing.getTotalSlot())
                .build()).toList();
    }
}

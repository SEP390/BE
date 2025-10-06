package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.repository.RoomPricingRepository;
import com.capstone.capstone.service.interfaces.IRoomPricingService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RoomPricingService implements IRoomPricingService {
    private final RoomPricingRepository roomPricingRepository;

    public List<RoomPricingResponse> getAllRoomPricing() {
        return roomPricingRepository.findAll(Sort.by(Sort.Direction.ASC, "price")).stream().map(pricing -> RoomPricingResponse.builder()
                .price(pricing.getPrice())
                .id(pricing.getId())
                .totalSlot(pricing.getTotalSlot())
                .build()).toList();
    }

    public long getPriceOfRoom(Room room) {
        return roomPricingRepository.findByRoom(room).getPrice();
    }
}

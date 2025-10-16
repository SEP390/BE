package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.room.RoomPricingRequest;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.repository.RoomPricingRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RoomPricingService {
    private final RoomPricingRepository roomPricingRepository;
    private final ModelMapper modelMapper;

    public List<RoomPricingResponse> getAll() {
        return roomPricingRepository.findAll(Sort.by(Sort.Direction.ASC, "price")).stream().map(pricing -> RoomPricingResponse.builder()
                .price(pricing.getPrice())
                .id(pricing.getId())
                .totalSlot(pricing.getTotalSlot())
                .build()).toList();
    }

    public long getPriceOfRoom(Room room) {
        return roomPricingRepository.findByRoom(room).getPrice();
    }

    public long getPriceOfSlot(Slot slot) {
        return roomPricingRepository.findBySlot(slot).getPrice();
    }

    public RoomPricingResponse create(RoomPricingRequest request) {
        if (roomPricingRepository.findByTotalSlot(request.getTotalSlot()) == null) {
            var newPricing = roomPricingRepository.save(modelMapper.map(request, RoomPricing.class));
            return modelMapper.map(newPricing, RoomPricingResponse.class);
        } else {
            throw new RuntimeException("Already existed!");
        }
    }

    public RoomPricingResponse update(RoomPricingRequest request) {
        if (roomPricingRepository.findByTotalSlot(request.getTotalSlot()) != null) {
            var newPricing = roomPricingRepository.save(modelMapper.map(request, RoomPricing.class));
            return modelMapper.map(newPricing, RoomPricingResponse.class);
        } else {
            throw new RuntimeException("Not existed!");
        }
    }
}

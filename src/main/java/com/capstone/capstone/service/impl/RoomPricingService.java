package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.room.CreateRoomPricingRequest;
import com.capstone.capstone.dto.request.room.UpdateRoomPricingRequest;
import com.capstone.capstone.dto.response.room.RoomPricingResponse;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.RoomPricingRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class RoomPricingService {
    private final RoomPricingRepository roomPricingRepository;
    private final ModelMapper modelMapper;

    public List<RoomPricing> getAll() {
        return roomPricingRepository.findAll();
    }

    public List<RoomPricingResponse> getAll(Integer totalSlot) {
        return roomPricingRepository.findAll(
                totalSlot != null ? (r,q,c) -> c.equal(r.get("totalSlot"), totalSlot) : Specification.unrestricted(),
                Sort.by(Sort.Direction.ASC, "price")
        ).stream().map(pricing -> modelMapper.map(pricing, RoomPricingResponse.class)).toList();
    }

    /**
     * Get pricing of Room
     * @param room room to get pricing
     * @return pricing
     */
    public Optional<RoomPricing> getByRoom(Room room) {
        return roomPricingRepository.findByRoom(room);
    }

    /**
     * Get pricing of slot
     * @param slot
     * @return price
     */
    public Optional<RoomPricing> getBySlot(Slot slot) {
        return roomPricingRepository.findBySlot(slot);
    }

    public RoomPricingResponse create(CreateRoomPricingRequest request) {
        RoomPricing pricing = modelMapper.map(request, RoomPricing.class);
        return modelMapper.map(create(pricing), RoomPricingResponse.class);
    }

    public RoomPricing getOrCreate(Integer totalSlot) {
        RoomPricing pricing = getByTotalSlot(totalSlot).orElse(null);
        if (pricing == null) {
            pricing = create(RoomPricing.builder().totalSlot(totalSlot).price(0L).build());
        }
        return pricing;
    }

    public RoomPricing create(RoomPricing pricing) {
        if (pricing.getId() != null) pricing.setId(null);
        if (roomPricingRepository.findByTotalSlot(pricing.getTotalSlot()).isPresent()) {
            throw new AppException("TOTAL_SLOT_EXISTED");
        }
        return roomPricingRepository.save(pricing);
    }

    public RoomPricingResponse update(UUID id, UpdateRoomPricingRequest request) {
        var pricing = roomPricingRepository.findById(id).orElseThrow(() -> new AppException("PRICING_NOT_FOUND"));
        pricing.setPrice(request.getPrice());
        pricing = roomPricingRepository.save(pricing);
        return modelMapper.map(pricing, RoomPricingResponse.class);
    }

    public Optional<RoomPricing> getByTotalSlot(Integer totalSlot) {
        return roomPricingRepository.findByTotalSlot(totalSlot);
    }

    public RoomPricingResponse getById(UUID id) {
        var pricing = roomPricingRepository.findById(id).orElseThrow(() -> new AppException("PRICING_NOT_FOUND"));
        return modelMapper.map(pricing, RoomPricingResponse.class);
    }
}

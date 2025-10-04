package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.response.room.RoomPricingResponse;

import java.util.List;

public interface IRoomPricingService {
    List<RoomPricingResponse> getAllRoomPricing();
}

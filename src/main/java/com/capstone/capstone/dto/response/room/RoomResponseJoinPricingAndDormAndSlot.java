package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.response.slot.SlotResponse;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Room join fetch Dorm, Slots, RoomPricing
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomResponseJoinPricingAndDormAndSlot extends RoomResponse {
    private RoomPricingResponse pricing;
    private DormResponse dorm;
    private List<SlotResponse> slots;
}

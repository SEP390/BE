package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.room.RoomResponseJoinPricingAndDorm;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SlotResponseJoinRoomAndDormAndPricing extends SlotResponse {
    private RoomResponseJoinPricingAndDorm room;
}

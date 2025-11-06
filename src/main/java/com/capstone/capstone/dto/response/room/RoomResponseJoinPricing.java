package com.capstone.capstone.dto.response.room;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Room join RoomPricing
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomResponseJoinPricing extends RoomResponse {
    private RoomPricingResponse pricing;
}

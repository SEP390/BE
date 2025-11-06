package com.capstone.capstone.dto.response.room;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Room join fetch RoomPricing, Dorm
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomResponseJoinPricingAndDorm extends RoomResponseJoinDorm {
    private RoomPricingResponse pricing;
}

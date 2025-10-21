package com.capstone.capstone.dto.response.room;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Room join fetch RoomPricing, Dorm
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomPriceDormResponse extends RoomDormResponse {
    private RoomPricingResponse pricing;
}

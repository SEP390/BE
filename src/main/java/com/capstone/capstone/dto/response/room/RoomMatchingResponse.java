package com.capstone.capstone.dto.response.room;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoomMatchingResponse extends RoomResponseJoinPricingAndDormAndSlot {
    private Double matching;
}

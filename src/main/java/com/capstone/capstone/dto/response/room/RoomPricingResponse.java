package com.capstone.capstone.dto.response.room;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RoomPricingResponse {
    private UUID id;
    private long price;
    private int totalSlot;
}

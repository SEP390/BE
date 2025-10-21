package com.capstone.capstone.dto.response.room;

import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoomMatchingResponse extends RoomPriceDormResponse {
    private Double matching;
}

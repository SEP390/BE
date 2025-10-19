package com.capstone.capstone.dto.request.room;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateRoomRequest {
    private String roomNumber;
    private Integer totalSlot;
    private Integer floor;

    private UUID dormId;
}

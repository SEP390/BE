package com.capstone.capstone.dto.request.room;

import lombok.Data;

import java.util.UUID;

@Data
public class RoomMatchingRequest {
    private int totalSlot;
    private int floor;
    private UUID dormId;
}

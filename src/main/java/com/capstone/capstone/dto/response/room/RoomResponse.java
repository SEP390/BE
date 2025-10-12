package com.capstone.capstone.dto.response.room;

import lombok.Data;

import java.util.UUID;

@Data
public class RoomResponse {
    private UUID id;
    private String roomNumber;
    private Integer floor;
}

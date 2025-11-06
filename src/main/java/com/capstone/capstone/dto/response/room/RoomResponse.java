package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import lombok.Data;

import java.util.UUID;

/**
 * Core response, no relations
 */
@Data
public class RoomResponse {
    private UUID id;
    private String roomNumber;
    private Integer floor;
    private Integer totalSlot;
    private StatusRoomEnum status;
}

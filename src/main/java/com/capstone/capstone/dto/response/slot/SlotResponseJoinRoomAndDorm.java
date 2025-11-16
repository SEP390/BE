package com.capstone.capstone.dto.response.slot;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Slot join Room, Dorm
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SlotResponseJoinRoomAndDorm extends SlotResponse {
    private RoomResponseJoinDorm room;
}

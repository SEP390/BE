package com.capstone.capstone.dto.response.dorm;

import com.capstone.capstone.dto.response.room.RoomResponseJoinSlot;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Dorm join fetch Room, Room join fetch Slot
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormResponseJoinRoomSlot extends DormResponse {
    private List<RoomResponseJoinSlot> rooms;
}

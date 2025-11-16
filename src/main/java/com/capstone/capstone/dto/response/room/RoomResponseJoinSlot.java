package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.response.slot.SlotResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Room join fetch Slot response
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomResponseJoinSlot extends RoomResponse {
    private List<SlotResponse> slots;
}

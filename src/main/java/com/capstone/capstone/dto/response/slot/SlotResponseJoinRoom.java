package com.capstone.capstone.dto.response.slot;

import com.capstone.capstone.dto.response.room.RoomResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SlotResponseJoinRoom extends SlotResponse {
    private RoomResponse room;
}

package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SlotRoomDormResponse extends SlotResponse {
    private RoomResponseJoinDorm room;
}

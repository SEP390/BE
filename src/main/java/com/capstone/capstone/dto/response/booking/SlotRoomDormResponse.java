package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.room.RoomDormResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SlotRoomDormResponse extends SlotResponse {
    private RoomDormResponse room;
}

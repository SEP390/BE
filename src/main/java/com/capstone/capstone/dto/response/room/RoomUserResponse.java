package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.response.booking.SlotResponse;
import com.capstone.capstone.dto.response.user.CoreUserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoomUserResponse extends CoreUserResponse {
    private SlotResponse slot;
}

package com.capstone.capstone.dto.request.room;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import lombok.Data;

@Data
public class UpdateRoomRequest {
    private String roomNumber;
    private Integer floor;
    private StatusRoomEnum status;
    private Integer totalSlot;
}

package com.capstone.capstone.dto.request.room;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateRoomRequest {
    private UUID roomId;
    private String roomNumber;
    private Integer floor;
    private StatusRoomEnum status;
}

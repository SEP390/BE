package com.capstone.capstone.dto.response.ew;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EWRoomResponse {
    private RoomResponseJoinDorm room;
    private Integer electric;
    private Integer water;
    private LocalDateTime createTime;
}

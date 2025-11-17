package com.capstone.capstone.dto.slotHistory;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SlotHistoryResponse {
    private SemesterResponse semester;
    private RoomResponseJoinDorm room;
    private UUID slotId;
    private String slotName;
    private LocalDateTime checkin;
    private LocalDateTime checkout;
}

package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

@Data
public class SlotHistoryResponse {
    private SlotRoomDormResponse slot;
    private SemesterResponse semester;
}

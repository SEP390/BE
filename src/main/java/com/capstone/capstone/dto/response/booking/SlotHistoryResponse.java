package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

@Data
public class SlotHistoryResponse {
    private SlotResponseJoinRoomAndDorm slot;
    private SemesterResponse semester;
}

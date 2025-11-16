package com.capstone.capstone.dto.slotHistory;

import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

import java.util.UUID;

@Data
public class SlotHistoryResponse {
    private UUID slotId;
    private UUID roomId;
    private UUID dormId;
    private String slotName;
    private String roomNumber;
    private String dormName;
    private SemesterResponse semester;
}

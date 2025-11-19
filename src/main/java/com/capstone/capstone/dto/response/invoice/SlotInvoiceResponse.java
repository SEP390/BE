package com.capstone.capstone.dto.response.invoice;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import com.capstone.capstone.dto.response.semester.SemesterResponse;
import lombok.Data;

import java.util.UUID;

@Data
public class SlotInvoiceResponse {
    private SemesterResponse semester;
    private RoomResponseJoinDorm room;
    private String slotName;
    private UUID slotId;
    private Long price;
}

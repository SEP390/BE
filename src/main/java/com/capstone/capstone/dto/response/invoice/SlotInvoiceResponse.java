package com.capstone.capstone.dto.response.invoice;

import com.capstone.capstone.dto.response.room.RoomResponseJoinDorm;
import lombok.Data;

@Data
public class SlotInvoiceResponse {
    private String semesterName;
    private RoomResponseJoinDorm room;
    private String slotName;
    private Long price;
}

package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class SlotResponse {
    private UUID id;
    private String slotName;
    private StatusSlotEnum status;
}

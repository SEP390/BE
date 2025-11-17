package com.capstone.capstone.dto.response.slot;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import lombok.Data;

import java.util.UUID;

/**
 * Slot core response (no relation)
 */
@Data
public class SlotResponse {
    private UUID id;
    private String slotName;
    private StatusSlotEnum status;
}

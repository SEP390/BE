package com.capstone.capstone.dto.request.slot;

import lombok.Data;

import java.util.UUID;

@Data
public class SwapSlotRequest {
    private UUID userId;
    private UUID slotId;
}

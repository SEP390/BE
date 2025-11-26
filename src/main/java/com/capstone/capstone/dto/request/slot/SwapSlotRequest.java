package com.capstone.capstone.dto.request.slot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwapSlotRequest {
    private UUID userId;
    private UUID slotId;
}

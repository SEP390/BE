package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SlotHistoryResponse {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID slotId;
    private LocalDateTime createdDate;
    private String transactionDate;
    private StatusSlotHistoryEnum status;
}

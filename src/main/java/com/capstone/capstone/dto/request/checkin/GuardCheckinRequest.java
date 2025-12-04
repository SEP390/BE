package com.capstone.capstone.dto.request.checkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardCheckinRequest {
    private UUID slotId;
    private String note;
}

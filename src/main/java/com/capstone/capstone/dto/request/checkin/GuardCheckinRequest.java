package com.capstone.capstone.dto.request.checkin;

import lombok.Data;

import java.util.UUID;

@Data
public class GuardCheckinRequest {
    private UUID slotId;
}

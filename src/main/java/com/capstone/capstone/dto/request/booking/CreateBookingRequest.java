package com.capstone.capstone.dto.request.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBookingRequest {
    @NotNull(message = "SLOT_ID_NULL")
    private UUID slotId;
}

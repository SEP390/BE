package com.capstone.capstone.dto.request.room;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoomRequest {
    @NotNull(message = "ROOM_NUMBER_NULL")
    @Size(max = 255, message = "ROOM_NUMBER_MAX")
    private String roomNumber;
    @NotNull(message = "TOTAL_SLOT_NULL")
    @Min(value = 1, message = "TOTAL_SLOT_MIN")
    @Max(value = 255, message = "TOTAL_SLOT_MAX")
    private Integer totalSlot;
    @NotNull(message = "FLOOR_NULL")
    private Integer floor;
}

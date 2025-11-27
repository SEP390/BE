package com.capstone.capstone.dto.request.ew;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateEWRoomRequest {
    private UUID id;
    private Integer electric;
    private Integer water;
}

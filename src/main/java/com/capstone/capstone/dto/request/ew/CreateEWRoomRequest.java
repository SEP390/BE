package com.capstone.capstone.dto.request.ew;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateEWRoomRequest {
    private UUID roomId;
    private Integer electric;
    private Integer water;
}

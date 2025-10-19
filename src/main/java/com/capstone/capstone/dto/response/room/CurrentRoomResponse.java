package com.capstone.capstone.dto.response.room;

import lombok.Data;

import java.util.UUID;

@Data
public class CurrentRoomResponse {
    private UUID id;
    private String roomNumber;
    private DormDto dorm;
    private Integer totalSlot;
    private Integer floor;
    private Long price;

    @Data
    public static class DormDto {
        private UUID id;
        private String dormName;
    }
}

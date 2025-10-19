package com.capstone.capstone.dto.request.dorm;

import lombok.Data;

import java.util.List;

@Data
public class CreateDormRequest {
    private String dormName;
    private Integer totalFloor;
    private Integer totalRoom;
    private List<RoomRequest> rooms;

    @Data
    public static class RoomRequest {
        private String roomNumber;
        private Integer totalSlot;
        private Integer floor;
    }
}

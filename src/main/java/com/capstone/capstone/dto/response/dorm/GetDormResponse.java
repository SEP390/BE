package com.capstone.capstone.dto.response.dorm;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GetDormResponse {
    private UUID id;
    private String dormName;
    private Integer totalFloor;
    private Integer totalRoom;
    private List<RoomResponse> rooms;

    @Data
    public static class RoomResponse {
        private UUID id;
        private String roomNumber;
        private Integer totalSlot;
        private Integer floor;
        private StatusRoomEnum status;
        private List<SlotResponse> slots;
    }

    @Data
    private static class SlotResponse {
        private UUID id;
        private String slotName;
        private StatusSlotEnum status;
    }
}

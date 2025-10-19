package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RoomDetailsResponse {
    private UUID id;
    private String roomNumber;
    private DormResponse dorm;
    private long pricing;
    private List<SlotResponse> slots;

    @Data
    public static class DormResponse {
        private UUID id;
        private String dormName;
    }

    @Data
    public static class SlotResponse {
        private UUID id;
        private String slotName;
        private StatusSlotEnum status;
    }
}

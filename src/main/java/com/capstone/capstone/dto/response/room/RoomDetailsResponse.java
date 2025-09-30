package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
public class RoomDetailsResponse {
    private UUID id;
    private String roomNumber;
    private DormResponse dorm;
    private long pricing;
    private List<SlotResponse> slots;

    @Getter
    @Setter
    @Builder
    @ToString
    public static class DormResponse {
        private String dormName;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class SlotResponse {
        private String slotName;
        private StatusSlotEnum status;
    }
}

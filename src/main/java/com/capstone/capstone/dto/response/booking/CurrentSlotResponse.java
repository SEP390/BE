package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class CurrentSlotResponse {
    private String slotName;
    private StatusSlotEnum status;
    private RoomDto room;
    private SemesterDto semester;
    private Long price;

    @Data
    public static class RoomDto {
        private UUID id;
        private String roomNumber;
        private Integer floor;
        private DormDto dorm;
    }

    @Data
    public static class DormDto {
        private UUID id;
        private String dormName;
    }

    @Data
    public static class SemesterDto {
        private UUID id;
        private String semesterName;
    }
}

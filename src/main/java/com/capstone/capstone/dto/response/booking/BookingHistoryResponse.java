package com.capstone.capstone.dto.response.booking;

import com.capstone.capstone.dto.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingHistoryResponse {
    private UUID id;
    private SlotHistoryDto slotHistory;
    private SemesterDto semester;
    private Long price;
    private LocalDateTime createDate;
    private PaymentStatus status;

    @Data
    private static class SlotHistoryDto {
        private SlotDto slot;
    }

    @Data
    public static class SemesterDto {
        private UUID id;
        private String name;
    }

    @Data
    public static class SlotDto {
        private UUID id;
        private String slotName;
        private RoomDto room;
    }

    @Data
    public static class RoomDto {
        private UUID id;
        private String roomNumber;
        private DormDto dorm;
        private Integer floor;
    }

    @Data
    public static class DormDto {
        private UUID id;
        private String dormName;
    }
}

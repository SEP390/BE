package com.capstone.capstone.dto.response.room;

import lombok.*;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RoomMatchingResponse {
    private UUID id;
    private String roomNumber;
    private UUID dormId;
    private String dormName;
    private int floor;
    private long price;
    private double matching;
    private int slotAvailable;
}

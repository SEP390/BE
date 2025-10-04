package com.capstone.capstone.dto.response.room;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RoomMatchingResponse {
    private UUID id;
    private String roomNumber;
    private String dormName;
    private int floor;
    private double matching;
    private int slotAvailable;
}

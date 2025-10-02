package com.capstone.capstone.dto.response.dorm;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BookableDormResponse {
    private UUID id;
    private String dormName;
    private int totalRoom;
    private int totalFloor;
    private boolean status;
}

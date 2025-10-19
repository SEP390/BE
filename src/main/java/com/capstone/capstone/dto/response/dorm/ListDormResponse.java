package com.capstone.capstone.dto.response.dorm;

import lombok.Data;

import java.util.UUID;

@Data
public class ListDormResponse {
    private UUID id;
    private String dormName;
    private Integer totalFloor;
    private Integer totalRoom;
}

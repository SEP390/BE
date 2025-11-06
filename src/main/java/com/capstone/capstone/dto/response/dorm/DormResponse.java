package com.capstone.capstone.dto.response.dorm;

import lombok.Data;

import java.util.UUID;

/**
 * Core Dorm response, no relations
 */
@Data
public class DormResponse {
    private UUID id;
    private String dormName;
    private Integer totalFloor;
    private Integer totalRoom;
}

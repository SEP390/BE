package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.response.dorm.DormResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Room join fetch Dorm response
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomDormResponse extends RoomResponse {
    private DormResponse dorm;
}

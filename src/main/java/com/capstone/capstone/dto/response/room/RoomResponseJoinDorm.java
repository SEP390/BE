package com.capstone.capstone.dto.response.room;

import com.capstone.capstone.dto.response.dorm.DormResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Room join fetch Dorm
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomResponseJoinDorm extends RoomResponse {
    private DormResponse dorm;
}

package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.response.room.RoomMatchingResponse;

import java.util.List;
import java.util.UUID;

public interface IRoomService {
    List<RoomMatchingResponse> getBookableRoomFirstYear(UUID currentUserId);
}

package com.capstone.capstone.dto.response.room;

import java.util.UUID;

public interface RoomMatching {
    UUID getRoomId();

    Integer getUserCount();

    Integer getSameOptionCount();
}

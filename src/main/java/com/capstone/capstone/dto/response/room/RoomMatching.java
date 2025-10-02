package com.capstone.capstone.dto.response.room;

import java.util.UUID;

public interface RoomMatching {
    UUID getId();

    String getDormName();

    int getFloor();

    String getRoomNumber();

    Double getMatching();

    int getSlotAvailable();
}

package com.capstone.capstone.dto.response.room;

import java.util.UUID;

public interface RoomDetails {
    UUID getId();

    String getRoomNumber();

    UUID getDormId();

    String getDormName();

    int getFloor();

    long getPrice();

    int getTotalSlot();
}

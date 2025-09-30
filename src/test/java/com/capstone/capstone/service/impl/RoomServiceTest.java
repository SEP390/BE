package com.capstone.capstone.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class RoomServiceTest {
    @Autowired
    RoomService roomService;

    @Test
    public void getBookableRoomFirstYear() {
        UUID currentUserId = UUID.fromString("0d7628b8-a61b-41c4-914f-6cd17ad373fc");
        roomService.getBookableRoomFirstYear(currentUserId).forEach(room -> {
            log.info("Room number: {}, Matching: {}", room.getRoomNumber(), room.getMatching());
        });
    }
}

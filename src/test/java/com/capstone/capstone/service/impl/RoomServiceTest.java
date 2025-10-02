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
        UUID currentUserId = UUID.fromString("a1778c64-32c1-46d0-b08b-a95d22038b8e");
        roomService.getBookableRoomFirstYear(currentUserId).forEach(room -> {
            log.info("Room number: {}, Matching: {}", room.getRoomNumber(), room.getMatching());
        });
    }

    @Test
    public void getBookableRoom() {
        UUID currentUserId = UUID.fromString("0d7628b8-a61b-41c4-914f-6cd17ad373fc");
        UUID dormId = UUID.fromString("353068cc-51b1-4cac-bb52-464b7c2af9af");
        roomService.getBookableRoom(currentUserId, 6, dormId, 1).forEach(room -> {
            log.info("Room number: {}, Matching: {}", room.getRoomNumber(), room.getMatching());
        });
    }

    @Test
    public void getRoomDetails() {
        UUID id = UUID.fromString("10165ca5-3d16-4eee-8164-f5a8f1ae8a1e");
        log.info("Room: {}", roomService.getRoomDetails(id));
    }
}

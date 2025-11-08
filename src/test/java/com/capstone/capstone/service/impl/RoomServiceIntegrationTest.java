package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "classpath:db/RoomServiceIntegrationTest.sql")
class RoomServiceIntegrationTest {
    @Autowired
    RoomService roomService;

    Room room;

    @BeforeEach
    void setup(@Autowired RoomRepository roomRepository) {
        room = roomRepository.findAll().getFirst();
    }

    @Test
    void getUsersResponse() {
        log.info("getUsersResponse: {}", roomService.getUsersResponse(room.getId()));
    }
}
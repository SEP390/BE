package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "classpath:db/SlotHistoryServiceTest.sql")
class SlotHistoryServiceTest {
    @Autowired
    SlotHistoryService slotHistoryService;

    Slot slot;
    Dorm dorm;
    Room room;
    Semester semester;
    Semester semester2;
    User user;
    RoomPricing roomPricing;

    @BeforeEach
    void setup(
            @Autowired RoomPricingRepository roomPricingRepository,
            @Autowired UserRepository userRepository,
            @Autowired SlotRepository slotRepository,
            @Autowired DormRepository dormRepository,
            @Autowired SemesterRepository semesterRepository,
            @Autowired RoomRepository roomRepository
    ) {
        roomPricing = roomPricingRepository.findAll().getFirst();
        user = userRepository.findAll().getFirst();
        dorm = dormRepository.findAll().getFirst();
        slot = slotRepository.findAll().getFirst();
        room = roomRepository.findAll().getFirst();
        semester = semesterRepository.findOne((r, q, c) -> c.equal(r.get("name"), "Semester Test")).orElse(null);
        semester2 = semesterRepository.findOne((r, q, c) -> c.equal(r.get("name"), "Semester Test 2")).orElse(null);
    }

    @Test
    void has_True() {
        boolean hasHistory = slotHistoryService.has(user, semester);
        assertThat(hasHistory).isEqualTo(true);
    }

    @Test
    void has_False() {
        boolean hasHistory = slotHistoryService.has(user, semester2);
        assertThat(hasHistory).isEqualTo(false);
    }

    @Test
    void getRooms() {
        var rooms = slotHistoryService.getRooms(user, semester);
        assertThat(rooms).isNotNull();
        assertThat(rooms).isNotEmpty();
        assertThat(rooms.getFirst().getRoomNumber()).isEqualTo("Room Test");
    }

    @Test
    void getSlots() {
        var slots = slotHistoryService.getSlots(user, semester);
        assertThat(slots).isNotNull();
        assertThat(slots).isNotEmpty();
        assertThat(slots.getFirst().getSlotName()).isEqualTo("Slot Test");
    }
}
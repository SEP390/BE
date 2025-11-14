package com.capstone.capstone;

import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.service.impl.DormService;
import com.capstone.capstone.service.impl.RoomPricingService;
import com.capstone.capstone.service.impl.SemesterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("dev")
class TestDataBuilder {
    @Autowired
    DormService dormService;
    @Autowired
    RoomPricingService roomPricingService;
    @Autowired
    SemesterService semesterService;

    Random random = new Random();

    @Test
    void generate() {
        generateSemester();
        generateRoomPricing();
        generateDorm();
    }

    @Test
    void generateSemester() {
        semesterService.create("SP24", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));
        semesterService.create("SU24", LocalDate.of(2024, 5, 1), LocalDate.of(2024, 7, 31));
        semesterService.create("FA24", LocalDate.of(2024, 9, 1), LocalDate.of(2024, 11, 30));
        semesterService.create("SP25", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31));
        semesterService.create("SU25", LocalDate.of(2025, 5, 1), LocalDate.of(2025, 7, 31));
        semesterService.create("FA25", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 11, 30));
        semesterService.create("SU26", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
    }

    @Test
    void generateRoomPricing() {
        roomPricingService.create(6, 800000L);
        roomPricingService.create(4, 1000000L);
        roomPricingService.create(2, 1200000L);
    }

    private void generateRoom(Dorm dorm) {
        for (int floor = 1; floor <= dorm.getTotalFloor(); floor++) {
            for (int roomIndex = 1; roomIndex <= 9; roomIndex++) {
                Room room = new Room();
                String roomNumber = String.valueOf(dorm.getDormName().split(" ")[1]) + floor + "%02d".formatted(roomIndex);
                int totalSlot = random.nextInt(1, 4) * 2;
                room.setRoomNumber(roomNumber);
                room.setTotalSlot(totalSlot);
                room.setFloor(floor);
                dormService.addRoom(dorm, room);
            }
        }
    }

    @Test
    void generateDorm() {
        for (char c = 'A'; c <= 'F'; c++) {
            String dormName = "Dorm " + c;
            int totalFloor = random.nextInt(3, 5);
            Dorm dorm = dormService.create(dormName, totalFloor);
            generateRoom(dorm);
        }
    }
}

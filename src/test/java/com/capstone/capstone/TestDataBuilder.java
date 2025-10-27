package com.capstone.capstone;

import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.service.impl.DormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;

@SpringBootTest
@ActiveProfiles("dev")
class TestDataBuilder {
    @Autowired
    DormService dormService;

    Random random = new Random();

    @Test
    void generate() {
        for (char c = 'A'; c <= 'F'; c++) {
            CreateDormRequest request = new CreateDormRequest();
            request.setDormName("Dorm " + c);
            int totalFloor = random.nextInt(3, 5);
            request.setTotalFloor(totalFloor);
            DormResponse dormResponse = dormService.create(request);
            for (int floor = 1; floor <= totalFloor; floor++) {
                for (int roomNumber = 1; roomNumber <= 9; roomNumber++) {
                    CreateRoomRequest roomRequest = new CreateRoomRequest();
                    roomRequest.setRoomNumber(String.valueOf(c) + floor + "%02d".formatted(roomNumber));
                    int totalSlot = random.nextInt(1, 4) * 2;
                    roomRequest.setTotalSlot(totalSlot);
                    roomRequest.setFloor(floor);
                    dormService.addRoom(dormResponse.getId(), roomRequest);
                }
            }
        }
    }
}

package com.capstone.capstone;

import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.service.impl.DormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
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
            int totalRoom = 9 * totalFloor;
            request.setTotalRoom(totalRoom);
            request.setRooms(new ArrayList<>());
            for (int floor = 1; floor <= totalFloor; floor++) {
                for (int roomNumber = 1; roomNumber <= 9; roomNumber++) {
                    CreateDormRequest.RoomRequest roomRequest = new CreateDormRequest.RoomRequest();
                    roomRequest.setRoomNumber(c + floor + "%02d".formatted(roomNumber));
                    roomRequest.setTotalSlot(random.nextInt(1, 4) * 2);
                    roomRequest.setFloor(floor);
                    request.getRooms().add(roomRequest);
                }
            }
            dormService.create(request);
        }
    }
}

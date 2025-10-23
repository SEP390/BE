package com.capstone.capstone;

import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.service.impl.DormService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

@SpringBootTest
@ActiveProfiles("dev")
class TestDataBuilder {
    @Autowired
    DormService dormService;

    @Test
    void generate() {
        for (char c = 'A'; c <= 'F'; c++) {
            CreateDormRequest request = new CreateDormRequest();
            request.setDormName("Dorm " + c);
            request.setTotalFloor(3);
            request.setTotalRoom(9);
            request.setRooms(new ArrayList<>());
            for (int i = 0; i < 9; i++) {
                CreateDormRequest.RoomRequest roomRequest = new CreateDormRequest.RoomRequest();
                roomRequest.setRoomNumber(c + "%02d".formatted(i));
                roomRequest.setTotalSlot(4);
                roomRequest.setFloor(i / 3 + 1);
                request.getRooms().add(roomRequest);
            }
            dormService.create(request);
        }
    }
}

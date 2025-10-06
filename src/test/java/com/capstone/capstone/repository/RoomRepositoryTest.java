package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RoomRepositoryTest {
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    SlotRepository slotRepository;

    @Test
    void findAvailableForGender() {
        for (int i = 0; i < 3; i++) {
            var r = new Room();
            r.setRoomNumber("Room %s".formatted(i));
            r.setStatus(StatusRoomEnum.AVAILABLE);
            roomRepository.save(r);
            for(int j = 0; j < 3; j++) {
                var s = new Slot();
                s.setStatus(StatusSlotEnum.AVAILABLE);
                s.setRoom(r);
                s.setSlotName("S1");
                slotRepository.save(s);
            }
        }
        assertThat(roomRepository.findAvailableForGender(GenderEnum.MALE)).size().isEqualTo(3);
    }
}
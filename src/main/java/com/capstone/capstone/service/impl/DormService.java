package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.dto.response.dorm.DormRoomSlotResponse;
import com.capstone.capstone.dto.response.room.RoomResponse;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SlotRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DormService {
    private final DormRepository dormRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final SlotRepository slotRepository;

    public List<RoomResponse> getRooms(UUID dormId) {
        Room example = new Room();
        Dorm dormExample = new Dorm();
        dormExample.setId(dormId);
        example.setDorm(dormExample);
        List<Room> rooms = roomRepository.findAll(Example.of(example));
        return rooms.stream().map(r -> modelMapper.map(r, RoomResponse.class)).toList();
    }

    @Transactional
    public DormRoomSlotResponse create(CreateDormRequest request) {
        Dorm dorm = modelMapper.map(request,Dorm.class);
        dorm = dormRepository.save(dorm);
        for (Room room : dorm.getRooms()) {
            room.setStatus(StatusRoomEnum.AVAILABLE);
            room.setDorm(dorm);
        }
        roomRepository.saveAll(dorm.getRooms());
        for (Room room : dorm.getRooms()) {
            List<Slot> slots = new ArrayList<>();
            for (int i = 1; i <= room.getTotalSlot(); i++) {
                Slot slot = new Slot();
                slot.setRoom(room);
                slot.setSlotName("Slot %s".formatted(i));
                slot.setStatus(StatusSlotEnum.AVAILABLE);
                slots.add(slot);
            }
            slots = slotRepository.saveAll(slots);
            room.setSlots(slots);
        }
        return modelMapper.map(dorm, DormRoomSlotResponse.class);
    }

    public List<DormResponse> getAll() {
        return dormRepository.findAll().stream().map(dorm -> modelMapper.map(dorm, DormResponse.class)).toList();
    }

    public DormRoomSlotResponse getResponse(UUID id) {
        return modelMapper.map(dormRepository.findById(id), DormRoomSlotResponse.class);
    }
}

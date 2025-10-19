package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.response.dorm.GetDormResponse;
import com.capstone.capstone.dto.response.dorm.ListDormResponse;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
import com.capstone.capstone.dto.response.dorm.CreateDormResponse;
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

    public List<BookableDormResponse> getBookableDorm(int totalSlot, GenderEnum gender) {
        return dormRepository.getBookableDorm(totalSlot, gender);
    }

    public List<RoomResponse> getRooms(UUID dormId) {
        Room example = new Room();
        Dorm dormExample = new Dorm();
        dormExample.setId(dormId);
        example.setDorm(dormExample);
        List<Room> rooms = roomRepository.findAll(Example.of(example));
        return rooms.stream().map(r -> modelMapper.map(r, RoomResponse.class)).toList();
    }

    public CreateDormResponse create(CreateDormRequest request) {
        Dorm dorm = dormRepository.save(modelMapper.map(request,Dorm.class));
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
        return modelMapper.map(dorm, CreateDormResponse.class);
    }

    public List<ListDormResponse> getList() {
        return dormRepository.findAll().stream().map(dorm -> modelMapper.map(dorm, ListDormResponse.class)).toList();
    }

    public GetDormResponse getResponse(UUID id) {
        return modelMapper.map(dormRepository.findById(id), GetDormResponse.class);
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.dto.response.dorm.DormResponseJoinRoomSlot;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricing;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DormService {
    private final DormRepository dormRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final RoomService roomService;

    public List<RoomResponseJoinPricing> getRooms(UUID dormId) {
        Room example = new Room();
        Dorm dormExample = new Dorm();
        dormExample.setId(dormId);
        example.setDorm(dormExample);
        List<Room> rooms = roomRepository.findAll(Example.of(example));
        return rooms.stream().map(r -> modelMapper.map(r, RoomResponseJoinPricing.class)).toList();
    }

    @Transactional
    public DormResponseJoinRoomSlot create(CreateDormRequest request) {
        Dorm dorm = modelMapper.map(request,Dorm.class);
        dorm = dormRepository.save(dorm);
        for (Room room : dorm.getRooms()) {
            room.setStatus(StatusRoomEnum.AVAILABLE);
            room.setDorm(dorm);
        }
        roomService.create(dorm.getRooms());
        return modelMapper.map(dorm, DormResponseJoinRoomSlot.class);
    }

    public List<DormResponse> getAll() {
        return dormRepository.findAll().stream().map(dorm -> modelMapper.map(dorm, DormResponse.class)).toList();
    }

    public DormResponseJoinRoomSlot getResponse(UUID id) {
        return modelMapper.map(dormRepository.findById(id), DormResponseJoinRoomSlot.class);
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
import com.capstone.capstone.dto.response.room.RoomResponse;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DormService {
    private final DormRepository dormRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

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
}

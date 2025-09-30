package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.room.RoomMatching;
import com.capstone.capstone.dto.response.room.RoomMatchingResponse;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.service.interfaces.IRoomService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;

    public List<RoomMatchingResponse> getBookableRoomFirstYear(UUID currentUserId) {
        return roomRepository.findBookableRoomFirstYear(currentUserId).stream().map(m -> RoomMatchingResponse.builder()
                .id(m.getId())
                .roomNumber(m.getRoomNumber())
                .matching(Optional.ofNullable(m.getMatching()).orElse(0D))
                .build()).toList();
    }
}

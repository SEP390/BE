package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.room.RoomDetailsResponse;
import com.capstone.capstone.dto.response.room.RoomMatching;
import com.capstone.capstone.dto.response.room.RoomMatchingResponse;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.service.interfaces.IRoomService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;

    public List<RoomMatchingResponse> getBookableRoomFirstYear(UUID currentUserId) {
        return roomRepository.findBookableRoomFirstYear(currentUserId).stream().map(m -> RoomMatchingResponse.builder()
                .id(convertBytesToUUID(m.getId()))
                .roomNumber(m.getRoomNumber())
                .matching(Optional.ofNullable(m.getMatching()).orElse(0D))
                .build()).toList();
    }

    public RoomDetailsResponse getRoomDetails(UUID id) {
        Room room = roomRepository.findDetails(id);
        return RoomDetailsResponse.builder()
                .roomNumber(room.getRoomNumber())
                .id(room.getId())
                .dorm(RoomDetailsResponse.DormResponse.builder()
                        .dormName(room.getDorm().getDormName())
                        .build())
                .slots(room.getSlots().stream().map(slot -> RoomDetailsResponse.SlotResponse.builder()
                        .slotName(slot.getSlotName())
                        .status(slot.getStatus())
                        .build()).toList())
                .build();
    }

    public static UUID convertBytesToUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }
}

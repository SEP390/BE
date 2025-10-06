package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.room.RoomDetailsResponse;
import com.capstone.capstone.dto.response.room.RoomMatchingResponse;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.RoomPricing;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.repository.RoomPricingRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.service.interfaces.IRoomService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;
    private final RoomPricingRepository roomPricingRepository;

    public List<RoomMatchingResponse> getBookableRoomFirstYear(UUID currentUserId) {
        return roomRepository.findBookableRoomFirstYear(currentUserId).stream().map(m -> RoomMatchingResponse.builder()
                .id(m.getId())
                .dormName(m.getDormName())
                .floor(m.getFloor())
                .roomNumber(m.getRoomNumber())
                .slotAvailable(m.getSlotAvailable())
                .matching(Optional.ofNullable(m.getMatching()).orElse(0D))
                .build()).toList();
    }

    public List<RoomMatchingResponse> getBookableRoom(UUID currentUserId, int totalSlot, UUID dormId, int floor) {
        return roomRepository.findBookableRoom(currentUserId, totalSlot, dormId, floor).stream().map(m -> RoomMatchingResponse.builder()
                .id(m.getId())
                .dormName(m.getDormName())
                .floor(m.getFloor())
                .roomNumber(m.getRoomNumber())
                .slotAvailable(m.getSlotAvailable())
                .matching(Optional.ofNullable(m.getMatching()).orElse(0D))
                .build()).toList();
    }

    public RoomDetailsResponse getRoomDetails(UUID id) {
        Room room = roomRepository.findDetails(id);
        RoomPricing pricing = roomPricingRepository.findByTotalSlot(room.getTotalSlot());
        return RoomDetailsResponse.builder()
                .roomNumber(room.getRoomNumber())
                .id(room.getId())
                .pricing(pricing.getPrice())
                .dorm(RoomDetailsResponse.DormResponse.builder()
                        .id(room.getDorm().getId())
                        .dormName(room.getDorm().getDormName())
                        .build())
                .slots(room.getSlots().stream().map(slot -> RoomDetailsResponse.SlotResponse.builder()
                        .id(slot.getId())
                        .slotName(slot.getSlotName())
                        .status(slot.getStatus())
                        .build()).toList())
                .build();
    }

    public boolean isFull(Room room) {
        room = roomRepository.findSlots(room);
        var isFull = true;
        for (Slot slot : room.getSlots()) {
            if (slot.getStatus().equals(StatusSlotEnum.AVAILABLE)) {
                isFull = false;
                break;
            }
        }
        return isFull;
    }

    public void checkFullAndUpdate(Room room) {
        if (isFull(room)) {
            room.setStatus(StatusRoomEnum.FULL);
        } else {
            room.setStatus(StatusRoomEnum.AVAILABLE);
        }
        roomRepository.save(room);
    }
}

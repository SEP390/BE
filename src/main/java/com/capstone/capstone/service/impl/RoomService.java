package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.room.RoomDetailsResponse;
import com.capstone.capstone.dto.response.room.RoomMatchingResponse;
import com.capstone.capstone.dto.response.room.RoommateResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.RoomPricingRepository;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomPricingRepository roomPricingRepository;
    private final ModelMapper modelMapper;

    public List<RoomMatchingResponse> getRoomMatching(User user) {
        var rooms = roomRepository.findAvailableForGender(user.getGender());
        final int totalQuestion = roomRepository.totalQuestion();
        final Map<UUID, Double> matching = new HashMap<>();
        rooms.forEach(room -> {
            var users = roomRepository.findUsers(room);
            matching.put(room.getId(), users.stream().mapToDouble(u -> roomRepository.computeMatching(user, u, totalQuestion)).average().orElse(0.0));
        });
        Comparator<Room> comparator = Comparator.comparingDouble(o -> matching.get(o.getId()));
        rooms.sort(comparator.reversed());
        return roomRepository.findDetails(rooms.subList(0, 5)).stream().map(room -> RoomMatchingResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .dormId(room.getDormId())
                .dormName(room.getDormName())
                .floor(room.getFloor())
                .matching(matching.get(room.getId()))
                .totalSlot(room.getTotalSlot())
                .price(room.getPrice())
                .build()).toList();
    }

    public RoomDetailsResponse getRoomById(UUID id) {
        Room room = roomRepository.fetchDormAndSlots(id);
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

    public List<RoommateResponse> getRoommates(UUID id) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User current = new User();
        current.setId(userId);
        Room room = roomRepository.getReferenceById(id);
        int totalQuestion = roomRepository.totalQuestion();
        List<User> users = roomRepository.findUsers(room).stream().filter(user -> !user.getId().equals(userId)).toList();
        Map<UUID, Double> matching = users.stream().collect(Collectors.toMap(BaseEntity::getId, u -> roomRepository.computeMatching(current, u, totalQuestion)));
        return users.stream().map(user -> modelMapper.map(user, RoommateResponse.class)).map(rm -> {
            rm.setMatching(matching.get(rm.getId()));
            return rm;
        }).toList();
    }

    public boolean isFull(Room room) {
        room = roomRepository.fetchSlots(room);
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

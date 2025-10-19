package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.booking.UserMatching;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import com.capstone.capstone.util.SortUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomPricingRepository roomPricingRepository;
    private final ModelMapper modelMapper;
    private final RoomPricingService roomPricingService;
    private final UserRepository userRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final MatchingRepository matchingRepository;
    private final DormRepository dormRepository;
    private final SlotRepository slotRepository;

    @Transactional
    public List<RoomMatchingResponse> getRoomMatching(User user) {
        int ROOM_COUNT = 5;
        final long totalQuestion = surveyQuestionRepository.count();
        List<Room> rooms = roomRepository.findAvailableForGender(user.getGender());
        final Map<UUID, Double> matching = matchingRepository.computeRoomMatching(user, rooms)
                .stream()
                .collect(Collectors.toMap(RoomMatching::getRoomId, m -> (double) m.getSameOptionCount() / m.getUserCount() / totalQuestion * 100));
        Comparator<Room> comparator = Comparator.comparingDouble(o -> matching.getOrDefault(o.getId(), 0.0));
        Map<Integer, Long> pricing = roomPricingService.getAll().stream().collect(Collectors.toMap(RoomPricingResponse::getTotalSlot, RoomPricingResponse::getPrice));
        rooms.sort(comparator.reversed());
        return rooms.subList(0, ROOM_COUNT).stream().map(room -> RoomMatchingResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .dormId(room.getDorm().getId())
                .dormName(room.getDorm().getDormName())
                .floor(room.getFloor())
                .matching(matching.getOrDefault(room.getId(), 0.0))
                .totalSlot(room.getTotalSlot())
                .price(pricing.get(room.getTotalSlot()))
                .build()).toList();
    }

    @Transactional
    public RoomDetailsResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id).orElseThrow();
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

    @Transactional
    public List<RoommateResponse> getRoommates(UUID id) {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Room room = roomRepository.getReferenceById(id);
        long totalQuestion = surveyQuestionRepository.count();
        List<User> users = roomRepository.findUsers(room).stream().filter(u -> !u.getId().equals(user.getId())).collect(Collectors.toList());
        Map<UUID, Double> matching = matchingRepository.computeUserMatching(user, users).stream().collect(Collectors.toMap(UserMatching::getId, m -> m.getSameOptionCount() / totalQuestion * 100));
        return users
                .stream()
                .map(u -> modelMapper.map(u, RoommateResponse.class))
                .peek(rm -> rm.setMatching(matching.getOrDefault(rm.getId(), 0.0))).toList();
    }

    public boolean isFull(Room room) {
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

    public PagedModel<RoomResponse> get(@Nullable UUID dormId, Integer floor, Integer totalSlot, Pageable pageable) {
        int validPageSize = Math.min(pageable.getPageSize(), 100);
        Sort validSort = SortUtil.getSort(pageable, "roomNumber");
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), validPageSize, validSort);
        return new PagedModel<>(roomRepository.findAll(
                Specification.allOf(
                        dormId != null ? (root, query, cb) -> cb.equal(root.get("dorm").get("id"), dormId) : Specification.unrestricted(),
                        floor != null ? (root, query, cb) -> cb.equal(root.get("floor"), floor) : Specification.unrestricted(),
                        totalSlot != null ? (root, query, cb) -> cb.equal(root.get("totalSlot"), totalSlot) : Specification.unrestricted()
                ),
                validPageable
        ).map(room -> modelMapper.map(room, RoomResponse.class)));
    }

    @Transactional
    public CurrentRoomResponse current() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Slot slot = slotRepository.findOne((r, q, cb) -> cb.equal(r.get("user"), user)).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        CurrentRoomResponse response = modelMapper.map(slot.getRoom(), CurrentRoomResponse.class);
        response.setPrice(roomPricingService.getPriceOfRoom(slot.getRoom()));
        return response;
    }
}

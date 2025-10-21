package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.request.room.UpdateRoomRequest;
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
        final long totalQuestion = surveyQuestionRepository.count();
        List<Room> rooms = roomRepository.findAvailableForGender(user.getGender());
        final Map<UUID, Double> matching = matchingRepository.computeRoomMatching(user, rooms)
                .stream()
                .collect(Collectors.toMap(RoomMatching::getRoomId, m -> (double) m.getSameOptionCount() / m.getUserCount() / totalQuestion * 100));
        Comparator<Room> comparator = Comparator.comparingDouble(o -> matching.getOrDefault(o.getId(), 0.0));
        Map<Integer, RoomPricingResponse> pricing = roomPricingService.getAll().stream().collect(Collectors.toMap(RoomPricingResponse::getTotalSlot, r -> r));
        rooms.sort(comparator.reversed());
        return rooms.subList(0, Math.min(5, rooms.size())).stream().map(room -> modelMapper.map(room, RoomMatchingResponse.class)).peek(room -> {
            room.setMatching(matching.getOrDefault(room.getId(), 0.0));
            room.setPricing(pricing.get(room.getTotalSlot()));
        }).toList();
    }

    @Transactional
    public RoomPriceDormSlotResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id).orElseThrow();
        RoomPricing pricing = roomPricingRepository.findByRoom(room);
        RoomPriceDormSlotResponse response = modelMapper.map(room, RoomPriceDormSlotResponse.class);
        response.setPricing(modelMapper.map(pricing, RoomPricingResponse.class));
        return response;
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

    public PagedModel<RoomResponse> get(@Nullable UUID dormId, Integer floor, Integer totalSlot, String roomNumber, Pageable pageable) {
        int validPageSize = Math.min(pageable.getPageSize(), 100);
        Sort validSort = SortUtil.getSort(pageable, "dormId", "floor", "totalSlot", "roomNumber");
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), validPageSize, validSort);
        return new PagedModel<>(roomRepository.findAll(
                Specification.allOf(
                        dormId != null ? (root, query, cb) -> cb.equal(root.get("dorm").get("id"), dormId) : Specification.unrestricted(),
                        floor != null ? (root, query, cb) -> cb.equal(root.get("floor"), floor) : Specification.unrestricted(),
                        totalSlot != null ? (root, query, cb) -> cb.equal(root.get("totalSlot"), totalSlot) : Specification.unrestricted(),
                        roomNumber != null ? (root, query, cb) -> cb.like(root.get("roomNumber"), "%" + roomNumber + "%") : Specification.unrestricted()
                ),
                validPageable
        ).map(room -> modelMapper.map(room, RoomResponse.class)));
    }

    @Transactional
    public RoomDormResponse current() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Slot slot = slotRepository.findOne((r, q, cb) -> cb.equal(r.get("user"), user)).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return modelMapper.map(slot.getRoom(), RoomDormResponse.class);
    }

    @Transactional
    public RoomResponse create(CreateRoomRequest request) {
        Dorm dorm = dormRepository.findById(request.getDormId()).orElseThrow(() -> new AppException("DORM_NOT_FOUND"));
        Room room = new Room();
        room.setDorm(dorm);
        room.setFloor(request.getFloor());
        room.setTotalSlot(request.getTotalSlot());
        room.setRoomNumber(request.getRoomNumber());
        room.setStatus(StatusRoomEnum.AVAILABLE);
        room = roomRepository.save(room);
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i <= request.getTotalSlot(); i++) {
            Slot slot = new Slot();
            slot.setRoom(room);
            slot.setSlotName("Slot %s".formatted(i));
            slot.setStatus(StatusSlotEnum.AVAILABLE);
            slots.add(slot);
        }
        slotRepository.saveAll(slots);
        return modelMapper.map(room, RoomResponse.class);
    }

    @Transactional
    public RoomResponse update(UpdateRoomRequest request) {
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        room.setFloor(request.getFloor());
        room.setRoomNumber(request.getRoomNumber());
        room.setStatus(request.getStatus());
        room = roomRepository.save(room);
        return modelMapper.map(room, RoomResponse.class);
    }
}

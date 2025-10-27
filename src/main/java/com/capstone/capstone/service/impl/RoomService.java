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
    private final ModelMapper modelMapper;
    private final RoomPricingService roomPricingService;
    private final UserRepository userRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final MatchingRepository matchingRepository;
    private final DormRepository dormRepository;
    private final SlotRepository slotRepository;
    private final SlotService slotService;

    @Transactional
    public List<RoomMatchingResponse> getMatching() {
        User user = userRepository.findById(Objects.requireNonNull(AuthenUtil.getCurrentUserId())).orElseThrow();
        final long totalQuestion = surveyQuestionRepository.count();
        List<Room> rooms = roomRepository.findAvailableForGender(user.getGender());
        final Map<UUID, Double> matching = matchingRepository.computeRoomMatching(user, rooms)
                .stream()
                .collect(Collectors.toMap(RoomMatching::getRoomId, m -> (double) m.getSameOptionCount() / m.getUserCount() / totalQuestion * 100));
        Comparator<Room> comparator = Comparator.comparingDouble(o -> matching.getOrDefault(o.getId(), 0.0));
        rooms.sort(comparator.reversed());
        return rooms.subList(0, Math.min(5, rooms.size())).stream().map(room -> modelMapper.map(room, RoomMatchingResponse.class)).peek(room -> {
            room.setMatching(matching.getOrDefault(room.getId(), 0.0));
        }).toList();
    }

    @Transactional
    public RoomResponseJoinPricingAndDormAndSlot getResponseById(UUID id) {
        Room room = getById(id);
        if (room == null) throw new AppException("ROOM_NOT_FOUND");
        return modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class);
    }

    public Room getById(UUID id) {
        return roomRepository.findById(id).orElse(null);
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
        List<Slot> slots = slotRepository.findAll((r, q, c) -> c.equal(r.get("room"), room));
        return slots.stream().allMatch(slot -> slot.getStatus() == StatusSlotEnum.UNAVAILABLE);
    }

    public void checkFullAndUpdate(Room room) {
        if (isFull(room)) {
            room.setStatus(StatusRoomEnum.FULL);
        } else {
            room.setStatus(StatusRoomEnum.AVAILABLE);
        }
        roomRepository.save(room);
    }

    public PagedModel<RoomResponseJoinPricingAndDormAndSlot> get(@Nullable UUID dormId, Integer floor, Integer totalSlot, String roomNumber, Pageable pageable) {
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
        ).map(room -> modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class)));
    }

    @Transactional
    public PagedModel<RoomResponseJoinPricing> getBooking(@Nullable UUID dormId, Integer floor, Integer totalSlot, String roomNumber, Pageable pageable) {
        User user = userRepository.findById(Objects.requireNonNull(AuthenUtil.getCurrentUserId())).orElseThrow();
        List<Room> rooms = roomRepository.findAvailableForGender(user.getGender());
        int validPageSize = Math.min(pageable.getPageSize(), 100);
        Sort validSort = SortUtil.getSort(pageable, "dormId", "floor", "totalSlot", "roomNumber");
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), validPageSize, validSort);
        return new PagedModel<>(roomRepository.findAll(
                Specification.allOf(
                        dormId != null ? (root, query, cb) -> cb.equal(root.get("dorm").get("id"), dormId) : Specification.unrestricted(),
                        floor != null ? (root, query, cb) -> cb.equal(root.get("floor"), floor) : Specification.unrestricted(),
                        totalSlot != null ? (root, query, cb) -> cb.equal(root.get("totalSlot"), totalSlot) : Specification.unrestricted(),
                        roomNumber != null ? (root, query, cb) -> cb.like(root.get("roomNumber"), "%" + roomNumber + "%") : Specification.unrestricted(),
                        (r,q,c) -> r.in(rooms)
                ),
                validPageable
        ).map(room -> modelMapper.map(room, RoomResponseJoinPricing.class)));
    }

    @Transactional
    public RoomResponseJoinDorm current() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Slot slot = slotRepository.findOne((r, q, cb) -> cb.equal(r.get("user"), user)).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return modelMapper.map(slot.getRoom(), RoomResponseJoinDorm.class);
    }

    @Transactional
    public RoomResponse create(CreateRoomRequest request) {
        Dorm dorm = dormRepository.findById(request.getDormId()).orElseThrow(() -> new AppException("DORM_NOT_FOUND"));
        Room room = new Room();
        room.setDorm(dorm);
        room.setFloor(request.getFloor());
        RoomPricing pricing = roomPricingService.getByTotalSlot(request.getTotalSlot()).orElse(null);
        if (pricing == null) throw new AppException("ROOM_TYPE_NOT_EXIST");
        room.setPricing(pricing);
        room.setTotalSlot(request.getTotalSlot());
        room.setRoomNumber(request.getRoomNumber());
        room.setStatus(StatusRoomEnum.AVAILABLE);
        room = roomRepository.save(room);
        slotService.create(room);
        return modelMapper.map(room, RoomResponse.class);
    }

    public Room create(Room room) {
        room = roomRepository.save(room);
        slotService.create(room);
        return room;
    }

    public List<Room> create(List<Room> rooms) {
        rooms.forEach(room -> {
            RoomPricing pricing = roomPricingService.getByTotalSlot(room.getTotalSlot()).orElse(null);
            if (pricing == null) throw new AppException("ROOM_TYPE_NOT_EXIST");
            room.setPricing(pricing);
        });
        List<Room> saved = roomRepository.saveAll(rooms);
        saved.forEach(slotService::create);
        return saved;
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

    @Transactional
    public List<RoomUserResponse> getUsersResponse(UUID id) {
        return getById(id).getSlots().stream().map(Slot::getUser).filter(Objects::nonNull).map(user -> modelMapper.map(user, RoomUserResponse.class)).toList();
    }
}

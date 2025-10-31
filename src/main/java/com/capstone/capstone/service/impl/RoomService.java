package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.room.UpdateRoomRequest;
import com.capstone.capstone.dto.response.booking.UserMatching;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import com.capstone.capstone.util.SecurityUtils;
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
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final MatchingRepository matchingRepository;
    private final SlotRepository slotRepository;
    private final SlotService slotService;
    private final SlotHistoryService slotHistoryService;

    @Transactional
    public List<RoomMatchingResponse> getMatching() {
        User user = SecurityUtils.getCurrentUser();
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
        Room room = getById(id).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class);
    }

    public Optional<Room> getById(UUID id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public List<RoommateResponse> getRoommates(UUID id) {
        User user = SecurityUtils.getCurrentUser();
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
        User user = SecurityUtils.getCurrentUser();
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
        User user = SecurityUtils.getCurrentUser();
        Slot slot = slotRepository.findOne((r, q, cb) -> cb.equal(r.get("user"), user)).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return modelMapper.map(slot.getRoom(), RoomResponseJoinDorm.class);
    }

    public Room create(Room room) {
        if (room.getStatus() == null) room.setStatus(StatusRoomEnum.AVAILABLE);
        if (room.getTotalSlot() == null) room.setTotalSlot(2);
        if (room.getFloor() == null) room.setFloor(1);
        if (room.getDorm() == null) throw new AppException("DORM_NULL");
        final String roomNumber = room.getRoomNumber();
        final Dorm dorm = room.getDorm();
        // trong cùng 1 dorm, ko có 2 phòng cùng tên
        if (roomRepository.exists((r,q,c) -> c.and(
                c.equal(r.get("roomNumber"), roomNumber),
                c.equal(r.get("dorm"), dorm)
        ))) throw new AppException("ROOM_NUMBER_EXISTED");
        room.setPricing(roomPricingService.getOrCreate(room.getTotalSlot()));
        room = roomRepository.save(room);
        room.setSlots(slotService.create(room));
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
    public RoomResponseJoinPricingAndDormAndSlot update(UUID id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        Room updated = new Room();
        updated.setId(room.getId());
        updated.setDorm(room.getDorm());
        updated.setTotalSlot(request.getTotalSlot());
        updated.setFloor(request.getFloor());
        updated.setRoomNumber(request.getRoomNumber());
        updated.setStatus(request.getStatus());
        room = update(updated);
        return modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class);
    }

    public Room update(Room room) {
        if (room.getId() == null) throw new AppException("INVALID_ROOM");
        if (room.getStatus() == null) room.setStatus(StatusRoomEnum.AVAILABLE);
        final int currentTotalSlot = getById(room.getId()).orElseThrow().getTotalSlot();
        if (room.getFloor() == null) room.setFloor(1);
        if (room.getFloor() <= 0 || room.getFloor() > room.getDorm().getTotalFloor())
            throw new AppException("INVALID_FLOOR");
        final String roomNumber = room.getRoomNumber();
        final Dorm dorm = room.getDorm();
        final UUID roomId = room.getId();
        if (roomRepository.exists((r,q,c) -> c.and(
                c.equal(r.get("roomNumber"), roomNumber),
                c.equal(r.get("dorm"), dorm),
                c.notEqual(r.get("id"), roomId)
        ))) throw new AppException("ROOM_NUMBER_EXISTED");

        // đang có người ở, ko thể sửa slot
        if (!getUsers(room).isEmpty()) throw new AppException("ALREADY_HAVE_USERS");
        RoomPricing pricing = roomPricingService.getOrCreate(room.getTotalSlot());
        room.setPricing(pricing);
        room = roomRepository.save(room);
        if (currentTotalSlot != room.getTotalSlot()) {
            // xóa slot cũ?
            slotService.deleteByRoom(room);
            room.setSlots(slotService.create(room));
        } else {
            room.setSlots(slotService.getByRoom(room));
        }
        return room;
    }

    @Transactional
    public List<RoomUserResponse> getUsersResponse(UUID id) {
        Room room = getById(id).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return getUsers(room).stream().map(user -> modelMapper.map(user, RoomUserResponse.class)).toList();
    }

    public List<User> getUsers(Room room) {
        return roomRepository.findUsers(room);
    }

    public List<Room> getAllByDorm(Dorm dorm) {
        return roomRepository.findAll((r, q, c) -> c.equal(r.get("dorm"), dorm));
    }

    /**
     * Lock slot
     * @param slot slot
     * @param user user
     * @throws AppException SLOT_NOT_AVAILABLE
     */
    public void lockSlot(Slot slot, User user) {
        // đổi trạng thái slot
        slot = slotService.lock(slot, user);
        // đổi trạng thái room (nếu tất cả các slot đều unavailable)
        checkFullAndUpdate(slot.getRoom());
    }

    public void unlockSlot(Slot slot) {
        slot = slotService.unlock(slot);
        checkFullAndUpdate(slot.getRoom());
    }

    public Slot successSlot(Slot slot) {
        slot = slotService.success(slot);
        checkFullAndUpdate(slot.getRoom());
        return slot;
    }

    public Optional<Room> getByUser(User user) {
        return Optional.ofNullable(roomRepository.findByUser(user));
    }
}

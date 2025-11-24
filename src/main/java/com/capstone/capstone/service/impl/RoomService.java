package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.request.room.UpdateRoomRequest;
import com.capstone.capstone.dto.response.booking.UserMatching;
import com.capstone.capstone.dto.response.room.*;
import com.capstone.capstone.dto.response.slot.SlotResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SortUtil;
import com.capstone.capstone.util.SpecQuery;
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

import java.time.LocalDate;
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
    private final SemesterService semesterService;
    private final SurveySelectRepository surveySelectRepository;
    private final TimeConfigService timeConfigService;

    @Transactional
    public List<RoomMatchingResponse> getMatching() {
        User user = SecurityUtils.getCurrentUser();
        TimeConfig timeConfig = timeConfigService.getCurrent().orElseThrow(() -> new AppException("TIME_CONFIG_NOT_FOUND"));
        var today = LocalDate.now();
        // đã từng dặt phòng
        if (slotHistoryService.existsByUser(user)) {
            if (!(today.isBefore(timeConfig.getEndExtendDate()) && today.isAfter(timeConfig.getStartExtendDate())))
                throw new AppException("BOOKING_DATE_NOT_START");
        } else {
            if (!(today.isBefore(timeConfig.getEndBookingDate()) && today.isAfter(timeConfig.getStartBookingDate())))
                throw new AppException("BOOKING_DATE_NOT_START");
        }
        Semester nextSemester = semesterService.getNext();

        if (nextSemester == null) throw new AppException("NEXT_SEMESTER_NOT_FOUND");

        // không được đặt phòng nếu chưa làm survey
        if (!surveySelectRepository.exists((r, q, c) -> {
            return c.equal(r.get("user"), user);
        })) throw new AppException("SURVEY_NOT_FOUND");

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
        Room room = roomRepository.findById(id).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
        return modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class);
    }

    public Optional<Room> getById(UUID id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public List<RoommateResponse> getRoommates() {
        User user = SecurityUtils.getCurrentUser();
        Slot slot = Optional.ofNullable(user.getSlot()).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));
        Room room = slot.getRoom();
        long totalQuestion = surveyQuestionRepository.count();
        List<User> users = roomRepository.findUsers(room).stream().filter(u -> !u.getId().equals(user.getId())).collect(Collectors.toList());
        Map<UUID, Double> matching = matchingRepository.computeUserMatching(user, users).stream().collect(Collectors.toMap(UserMatching::getId, m -> m.getSameOptionCount() / totalQuestion * 100));
        return users
                .stream()
                .map(u -> modelMapper.map(u, RoommateResponse.class))
                .peek(rm -> rm.setMatching(matching.getOrDefault(rm.getId(), 0.0))).toList();
    }

    public void checkFullAndUpdate(Room room) {
        if (roomRepository.isFull(room)) {
            room.setStatus(StatusRoomEnum.FULL);
        } else {
            room.setStatus(StatusRoomEnum.AVAILABLE);
        }
        roomRepository.save(room);
    }

    public PagedModel<RoomResponseJoinPricingAndDormAndSlot> get(Map<String, Object> filter, Pageable pageable) {
        int validPageSize = Math.min(pageable.getPageSize(), 100);
        Sort validSort = SortUtil.getSort(pageable, "dormId", "floor", "totalSlot", "roomNumber");
        Pageable validPageable = PageRequest.of(pageable.getPageNumber(), validPageSize, validSort);
        var query = new SpecQuery<Room>();
        query.equal(r -> r.get("dorm").get("id"), filter.get("dormId"));
        query.equal(filter, "id");
        query.equal(filter, "floor");
        query.equal(filter, "totalSlot");
        query.like(filter, "roomNumber");
        return new PagedModel<>(roomRepository.findAll(query.and(), validPageable).map(room -> modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class)));
    }

    @Transactional
    public PagedModel<RoomResponseJoinPricingAndDormAndSlot> getBooking(@Nullable UUID dormId, Integer floor, Integer totalSlot, String roomNumber, Pageable pageable) {
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
                        (r, q, c) -> r.in(rooms)
                ),
                validPageable
        ).map(room -> modelMapper.map(room, RoomResponseJoinPricingAndDormAndSlot.class)));
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
        if (roomRepository.exists((r, q, c) -> c.and(
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
        if (roomRepository.exists((r, q, c) -> c.and(
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
        return room.getSlots().stream().map(Slot::getUser).filter(Objects::nonNull).map((user) -> {
            return modelMapper.map(user, RoomUserResponse.class);
        }).toList();
    }

    public List<User> getUsers(Room room) {
        return roomRepository.findUsers(room);
    }

    public List<Room> getAllByDorm(Dorm dorm) {
        return roomRepository.findAll((r, q, c) -> c.equal(r.get("dorm"), dorm));
    }

}

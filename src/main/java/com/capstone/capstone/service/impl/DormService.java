package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.DormStatus;
import com.capstone.capstone.dto.request.dorm.CreateDormRequest;
import com.capstone.capstone.dto.request.dorm.UpdateDormRequest;
import com.capstone.capstone.dto.request.room.CreateRoomRequest;
import com.capstone.capstone.dto.response.dorm.DormResponse;
import com.capstone.capstone.dto.response.dorm.DormResponseJoinRoomSlot;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricing;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricingAndDorm;
import com.capstone.capstone.entity.Dorm;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.RoomRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class DormService {
    private final DormRepository dormRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final RoomService roomService;

    /**
     * Create dorm
     *
     * @param request request
     * @return dorm
     */
    @Transactional
    public DormResponse create(CreateDormRequest request) {
        var dorm = modelMapper.map(request, Dorm.class);
        dorm.setTotalRoom(0);
        dorm = create(dorm);
        return modelMapper.map(dorm, DormResponse.class);
    }

    public Dorm create(String dormName, Integer totalFloor) {
        Dorm dorm = new Dorm();
        dorm.setDormName(dormName);
        dorm.setTotalFloor(totalFloor);
        return create(dorm);
    }

    public Dorm create(Dorm dorm) {
        if (dorm.getStatus() == null) dorm.setStatus(DormStatus.ACTIVE);
        if (dormRepository.exists((r, q, c) -> c.equal(r.get("dormName"), dorm.getDormName())))
            throw new AppException("DORM_NAME_EXISTED");
        return dormRepository.save(dorm);
    }

    /**
     * Get all dorm
     *
     * @return all dorm
     */
    @Transactional
    public List<DormResponse> getAllResponse() {
        return getAll().stream().map(dorm -> modelMapper.map(dorm, DormResponse.class)).toList();
    }

    public List<Dorm> getAll() {
        return dormRepository.findAll();
    }

    /**
     * get dorm by id
     *
     * @param id dorm id
     * @return dorm join room, slot
     */
    @Transactional
    public DormResponseJoinRoomSlot getResponseById(UUID id) {
        return modelMapper.map(dormRepository.findById(id), DormResponseJoinRoomSlot.class);
    }

    public Optional<Dorm> getById(UUID id) {
        return dormRepository.findById(id);
    }

    @Transactional
    public List<RoomResponseJoinPricing> getRooms(UUID dormId) {
        var dorm = getById(dormId).orElseThrow(() -> new AppException("DORM_NOT_FOUND"));
        var rooms = getRooms(dorm);
        return rooms.stream().map(r -> modelMapper.map(r, RoomResponseJoinPricing.class)).toList();
    }

    public List<Room> getRooms(Dorm dorm) {
        return roomService.getAllByDorm(dorm);
    }

    @Transactional
    public RoomResponseJoinPricingAndDorm addRoom(UUID id, CreateRoomRequest request) {
        var dorm = getById(id).orElseThrow(() -> new AppException("DORM_NOT_FOUND"));
        var room = modelMapper.map(request, Room.class);
        room = addRoom(dorm, room);
        return modelMapper.map(room, RoomResponseJoinPricingAndDorm.class);
    }

    public Room addRoom(Dorm dorm, Room room) {
        if (room.getFloor() <= 0 || room.getFloor() > dorm.getTotalFloor()) throw new AppException("INVALID_FLOOR");
        room.setDorm(dorm);
        room = roomService.create(room);
        dorm = updateTotalRoom(dorm);
        room.setDorm(dorm);
        return room;
    }

    @Transactional
    public DormResponse update(UUID id, UpdateDormRequest request) {
        Dorm dorm = getById(id).orElseThrow(() -> new AppException("DORM_NOT_FOUND"));
        Dorm updated = new Dorm();
        updated.setId(dorm.getId());
        updated.setDormName(request.getDormName());
        updated.setTotalFloor(request.getTotalFloor());
        updated.setTotalRoom(dorm.getTotalRoom());
        dorm = update(updated);
        return modelMapper.map(dorm, DormResponse.class);
    }

    public Dorm update(Dorm dorm) {
        if (dormRepository.exists((r, q, c) -> c.and(
                c.equal(r.get("dormName"), dorm.getDormName()),
                c.notEqual(r.get("id"), dorm.getId())
        ))) throw new AppException("DORM_NAME_EXISTED");
        return dormRepository.save(dorm);
    }

    public Dorm updateTotalRoom(Dorm dorm) {
        dorm.setTotalRoom((int) roomRepository.count((r, q, c) -> c.equal(r.get("dorm"), dorm)));
        return dormRepository.save(dorm);
    }
}

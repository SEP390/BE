package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.repository.SlotInvoiceRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final SlotHistoryRepository slotHistoryRepository;
    private final SlotInvoiceRepository slotInvoiceRepository;

    public Slot save(Slot slot) {
        return slotRepository.save(slot);
    }

    /**
     * Get slot by current user in it
     *
     * @param user user
     * @return slot
     */
    public Optional<Slot> getByUser(User user) {
        return Optional.ofNullable(slotRepository.findByUser(user));
    }

    /**
     * Get slot by current user in it
     */
    public SlotResponseJoinRoomAndDormAndPricing getCurrent() {
        User user = SecurityUtils.getCurrentUser();
        Slot slot = getByUser(user).orElseThrow();
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricing.class);
    }

    /**
     * Create slots for rooms (by totalSlot)
     *
     * @param room room
     * @return list slots
     */
    public List<Slot> create(Room room) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i <= room.getTotalSlot(); i++) {
            Slot slot = new Slot();
            slot.setRoom(room);
            slot.setSlotName("%s_%s".formatted(room.getRoomNumber(), i));
            slot.setStatus(StatusSlotEnum.AVAILABLE);
            slots.add(slot);
        }
        return slotRepository.saveAll(slots);
    }

    /**
     * Delete all slots in room
     *
     * @param room room
     */
    public void deleteByRoom(Room room) {
        // TODO: check room contains users
        slotRepository.deleteAllByRoom(room);
    }

    public List<Slot> getByRoom(Room room) {
        return slotRepository.findByRoom(room);
    }

    public SlotResponseJoinRoomAndDormAndPricingAndUser getResponseById(UUID id) {
        Slot slot = slotRepository.findById(id).orElseThrow();
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricingAndUser.class);
    }

    /**
     * Danh sách các slot đang chờ checkin
     * @param userCode mã sinh viên
     * @param pageable page, size
     * @return
     */
    public PagedModel<SlotResponseJoinRoomAndDormAndPricingAndUser> getAll(String userCode, StatusSlotEnum status, Pageable pageable) {
        return new PagedModel<>(slotRepository.findAll(Specification.allOf(userCode != null ? (r, q, c) -> {
            return c.like(r.get("user").get("userCode"), "%" + userCode + "%");
        } : Specification.unrestricted(), status != null ? (r,q,c) -> {
            return c.equal(r.get("status"), status);
        } : Specification.unrestricted()), pageable).map(s -> modelMapper.map(s, SlotResponseJoinRoomAndDormAndPricingAndUser.class)));
    }

    /**
     * [Guard] checkin slot cho sinh viên
     *
     * @param id id của slot
     * @return slot
     */
    public SlotResponseJoinRoomAndDormAndPricingAndUser checkin(UUID id) {
        Slot slot = slotRepository.findById(id).orElseThrow();
        User user = Optional.ofNullable(slot.getUser()).orElseThrow();
        // chuyển trạng thái sang unavailable
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        slot = slotRepository.save(slot);
        var his = slotHistoryRepository.getLatest(user, slot.getId()).orElse(null);
        if (his != null) {
            his.setCheckin(LocalDateTime.now());
            his = slotHistoryRepository.save(his);
        }
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricingAndUser.class);
    }

    public void lock(Slot slot, User user) {
        slot.setStatus(StatusSlotEnum.LOCK);
        slot.setUser(user);
        slot = slotRepository.save(slot);
        if (roomRepository.isFull(slot.getRoom())) {
            Room room = slot.getRoom();
            room.setStatus(StatusRoomEnum.FULL);
            roomRepository.save(room);
        }
    }

    public void unlock(Slot slot) {
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slot.setUser(null);
        slot = slotRepository.save(slot);
        if (!roomRepository.isFull(slot.getRoom())) {
            Room room = slot.getRoom();
            room.setStatus(StatusRoomEnum.AVAILABLE);
            roomRepository.save(room);
        }
    }

    public void book(Slot slot, Semester semester) {
        slot.setStatus(StatusSlotEnum.CHECKIN);
        SlotHistory slotHistory = new SlotHistory();
        slotRepository.save(slot);
        slotHistory.setSlotId(slot.getId());
        slotHistory.setSlotName(slot.getSlotName());
        slotHistory.setRoom(slot.getRoom());
        slotHistory.setUser(slot.getUser());
        slotHistory.setSemester(semester);
        slotHistoryRepository.save(slotHistory);
    }

    public SlotResponseJoinRoomAndDormAndPricingAndUser checkout(UUID id) {
        Slot slot = slotRepository.findById(id).orElseThrow();
        User user = Optional.ofNullable(slot.getUser()).orElseThrow();
        // chuyển trạng thái sang available
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        // xóa user
        slot.setUser(null);
        slot = slotRepository.save(slot);
        var his = slotHistoryRepository.getLatest(user, slot.getId()).orElse(null);
        if (his != null) {
            his.setCheckout(LocalDateTime.now());
            his = slotHistoryRepository.save(his);
        }
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricingAndUser.class);
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;

    public Optional<Slot> getById(UUID id) {
        return slotRepository.findById(id);
    }

    /**
     * Change slot status from available to lock for user
     *
     * @param slot slot
     * @param user user
     * @return slot
     * @throws AppException SLOT_NOT_AVAILABLE
     */
    public Slot fromAvailableToLock(Slot slot, User user) {
        // slot not available
        if (slot.getStatus() != StatusSlotEnum.AVAILABLE) throw new AppException("SLOT_NOT_AVAILABLE", slot.getId());
        // lock
        slot.setStatus(StatusSlotEnum.LOCK);
        // lock for user
        slot.setUser(user);
        return slotRepository.save(slot);
    }

    /**
     * Change slot status from lock to available
     *
     * @param slot slot
     * @return slot
     * @throws AppException SLOT_NOT_LOCKED
     */
    public Slot fromLockToAvailable(Slot slot) {
        // slot not locked
        if (slot.getStatus() != StatusSlotEnum.LOCK) throw new AppException("SLOT_NOT_LOCKED");
        // remove user
        slot.setUser(null);
        // change status to available
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        return slotRepository.save(slot);
    }

    /**
     * Change slot status from lock to unavailable (booking success)
     *
     * @param slot slot
     * @return slot
     * @throws AppException SLOT_NOT_LOCKED
     */
    public Slot fromLockToUnavailable(Slot slot) {
        if (slot.getStatus() != StatusSlotEnum.LOCK) throw new AppException("SLOT_NOT_LOCKED");
        // change to unavailable
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        return slotRepository.save(slot);
    }

    /**
     * Get slot by current user in it
     * @param user user
     * @return slot
     */
    public Optional<Slot> getByUser(User user) {
        return Optional.ofNullable(slotRepository.findByUser(user));
    }

    /**
     * Create slots for rooms (by totalSlot)
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
     * @param room room
     */
    public void deleteByRoom(Room room) {
        // TODO: check room contains users
        slotRepository.deleteAllByRoom(room);
    }

    public List<Slot> getByRoom(Room room) {
        return slotRepository.findByRoom(room);
    }

    public Slot removeUser(Slot slot) {
        if (slot.getUser() == null) throw new AppException("SLOT_EMPTY");
        slot.setUser(null);
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        return slotRepository.save(slot);
    }

    public Slot setUser(Slot slot, User user) {
        if (slot.getStatus() != StatusSlotEnum.AVAILABLE) throw new AppException("SLOT_NOT_AVAILABLE");
        if (slot.getUser() != null) throw new AppException("SLOT_NOT_AVAILABLE");
        slot.setUser(user);
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        return slotRepository.save(slot);
    }
}

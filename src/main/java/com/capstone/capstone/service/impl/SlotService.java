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
import java.util.UUID;

@Service
@AllArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;

    public Slot getById(UUID id) {
        return slotRepository.findById(id).orElse(null);
    }

    public Slot lock(Slot slot, User user) {
        // slot đã có người đặt
        if (slot.getStatus() == StatusSlotEnum.UNAVAILABLE) throw new AppException("SLOT_UNAVAILABLE", slot.getId());
        slot.setStatus(StatusSlotEnum.LOCK);
        slot.setUser(user);
        return slotRepository.save(slot);
    }

    public Slot unlock(Slot slot) {
        // slot không khóa
        if (slot.getStatus() != StatusSlotEnum.AVAILABLE) throw new AppException("SLOT_NOT_LOCKED");
        slot.setUser(null);
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        return slotRepository.save(slot);
    }

    public Slot success(Slot slot) {
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        return slotRepository.save(slot);
    }

    public Slot save(Slot slot) {
        return slotRepository.save(slot);
    }

    public Slot getByUser(User user) {
        return slotRepository.findByUser(user);
    }

    public List<Slot> create(Room room) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i <= room.getTotalSlot(); i++) {
            Slot slot = new Slot();
            slot.setRoom(room);
            slot.setSlotName("Slot %s".formatted(i));
            slot.setStatus(StatusSlotEnum.AVAILABLE);
            slots.add(slot);
        }
        return slotRepository.saveAll(slots);
    }
}

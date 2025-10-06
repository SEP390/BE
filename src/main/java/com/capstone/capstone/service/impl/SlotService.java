package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.SlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final RoomService roomService;

    public Slot getById(UUID id) {
        return slotRepository.findById(id).orElse(null);
    }

    public void lock(Slot slot) {
        if (slot.getStatus() == StatusSlotEnum.UNAVAILABLE) throw new RuntimeException("Slot already locked");
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        slotRepository.save(slot);
    }

    public List<Slot> getAllByRoom(Room room) {
        return slotRepository.findByRoom(room);
    }

    public void setUser(Slot slot, User user) {
        slot.setUser(user);
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        slotRepository.save(slot);
    }

    @Transactional
    public void unlock(Slot slot) {
        slot.setUser(null);
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slotRepository.save(slot);
        roomService.checkFullAndUpdate(slot.getRoom());
    }
}

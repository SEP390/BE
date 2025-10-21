package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final RoomService roomService;

    public Slot getById(UUID id) {
        return slotRepository.findById(id).orElse(null);
    }

    @Transactional
    public void lock(Slot slot, User user) {
        if (slot.getStatus() == StatusSlotEnum.UNAVAILABLE) throw new AppException("SLOT_UNAVAILABLE", slot.getId());
        slot.setStatus(StatusSlotEnum.LOCK);
        slot.setUser(user);
        slotRepository.save(slot);
        roomService.checkFullAndUpdate(slot.getRoom());
    }

    @Transactional
    public void unlock(Slot slot) {
        slot.setUser(null);
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slotRepository.save(slot);
        roomService.checkFullAndUpdate(slot.getRoom());
    }

    @Transactional
    public void unavailable(Slot slot) {
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        slotRepository.save(slot);
        roomService.checkFullAndUpdate(slot.getRoom());
    }

    public Slot save(Slot slot) {
        return slotRepository.save(slot);
    }

    public Slot getByUser(User user) {
        return slotRepository.findByUser(user);
    }

    @Transactional
    public void onPayment(Slot slot, VNPayStatus status) {
        if (status == VNPayStatus.SUCCESS) {
            unavailable(slot);
        } else {
            unlock(slot);
        }
    }
}

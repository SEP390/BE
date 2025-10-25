package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RoomSlotService {
    private final RoomService roomService;
    private final SlotService slotService;

    public void lockSlot(Slot slot, User user) {
        // đổi trạng thái slot
        slot = slotService.lock(slot, user);
        // đổi trạng thái room (nếu tất cả các slot đều unavailable)
        roomService.checkFullAndUpdate(slot.getRoom());
    }

    public void unlockSlot(Slot slot) {
        slot = slotService.unlock(slot);
        roomService.checkFullAndUpdate(slot.getRoom());
    }

    public void successSlot(Slot slot) {
        slot = slotService.success(slot);
        roomService.checkFullAndUpdate(slot.getRoom());
    }

    @Transactional
    public void onPayment(Payment payment, VNPayStatus status) {
        Slot slot = payment.getSlotHistory().getSlot();
        if (status == VNPayStatus.SUCCESS) {
            successSlot(slot);
        } else {
            unlockSlot(slot);
        }
    }
}

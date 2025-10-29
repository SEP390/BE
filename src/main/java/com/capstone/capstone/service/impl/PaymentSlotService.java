package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.PaymentSlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentSlotService {
    private final PaymentService paymentService;
    private final RoomService roomService;
    private final SlotService slotService;
    private final PaymentSlotRepository paymentSlotRepository;

    public PaymentSlot create(User user, Slot slot, Semester semester) {
        PaymentSlot paymentSlot = new PaymentSlot();
        paymentSlot.setUser(user);
        paymentSlot.setSemester(semester);
        paymentSlot.setSlotId(slot.getId());
        paymentSlot.setSlotName(slot.getSlotName());
        paymentSlot.setPrice(slot.getRoom().getPricing().getPrice());
        paymentSlot.setRoomNumber(slot.getRoom().getRoomNumber());
        paymentSlot.setDormName(slot.getRoom().getDorm().getDormName());
        paymentSlot = paymentSlotRepository.save(paymentSlot);
        Payment payment = paymentService.create(user, slot);
        paymentSlot.setPayment(payment);
        return paymentSlot;
    }

    public Optional<PaymentSlot> getByPayment(Payment payment) {
        return paymentSlotRepository.findOne((r,q,c) -> {
            return c.equal(r.get("payment"), payment);
        });
    }

    @Transactional
    public void onPayment(Payment payment) {
        PaymentSlot paymentSlot = getByPayment(payment).orElseThrow();
        Slot slot = slotService.getById(paymentSlot.getSlotId()).orElse(null);
        // slot deleted/invalid
        if (slot == null) return;
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            roomService.successSlot(slot);
        } else {
            roomService.unlockSlot(slot);
        }
    }
}

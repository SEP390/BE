package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.response.booking.SlotBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotHistoryResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotHistoryRepository slotHistoryRepository;
    private final SlotService slotService;
    private final PaymentService paymentService;

    @Transactional
    public SlotBookingResponse create(User user, UUID slotId) {
        // get slot
        Slot slot = slotService.getById(slotId);
        // create invoice
        Invoice invoice = paymentService.createForSlot(user, slot);
        // create payment url for invoice
        String paymentUrl = paymentService.createPaymentUrl(invoice);
        // lock slot (so other user cannot book this slot)
        slotService.lock(slot, user);
        // return url for frontend to redirect
        return new SlotBookingResponse(paymentUrl);
    }

    public List<SlotHistoryResponse> history(User user) {
        List<SlotHistory> shs = slotHistoryRepository.findAllByUser(user);
        return shs.stream().map(sh -> SlotHistoryResponse.builder()
                .semesterId(sh.getSemester().getId())
                .semesterName(sh.getSemester().getName())
                .dormId(sh.getSlot().getRoom().getDorm().getId())
                .dormName(sh.getSlot().getRoom().getDorm().getDormName())
                .roomId(sh.getSlot().getRoom().getId())
                .roomNumber(sh.getSlot().getRoom().getRoomNumber())
                .floor(sh.getSlot().getRoom().getFloor())
                .slotId(sh.getSlot().getId())
                .slotName(sh.getSlot().getSlotName())
                .createdDate(sh.getCreateDate())
                .build()).toList();
    }
}

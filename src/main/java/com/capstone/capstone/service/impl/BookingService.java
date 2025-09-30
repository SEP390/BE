package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.RoomPricingRepository;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final SlotHistoryRepository slotHistoryRepository;
    private final VNPayService vNPayService;
    private final RoomPricingRepository roomPricingRepository;

    public String createBooking(UUID currentUserId, UUID slotId) {
        Slot slot = slotRepository.getSlotWithRoom(slotId);
        long price = roomPricingRepository.findByTotalSlot(slot.getRoom().getTotalSlot()).getPrice();
        User user = userRepository.getReferenceById(currentUserId);
        String paymentUrl = vNPayService.createPaymentUrl(slotId, price);
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setSlot(slot);
        slotHistory.setCreatedDate(LocalDateTime.now());
        slotHistory.setUser(user);
        slotHistory.setStatus(StatusSlotHistoryEnum.PENDING);
        slotHistory.setPaymentUrl(paymentUrl);
        slotHistoryRepository.save(slotHistory);
        return paymentUrl;
    }
}

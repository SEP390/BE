package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.dto.response.booking.PaymentResultResponse;
import com.capstone.capstone.dto.response.booking.SlotBookingResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.RoomPricingRepository;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public SlotBookingResponse createBooking(UUID currentUserId, UUID slotId) {
        // lock slot (so other user cannot book this slot)
        Slot slot = slotRepository.findById(slotId).orElseThrow();
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);

        // create history
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setSlot(slot);
        var createDate = LocalDateTime.now();
        slotHistory.setCreateDate(createDate);
        User user = userRepository.getReferenceById(currentUserId);
        slotHistory.setUser(user);
        slotHistory.setStatus(StatusSlotHistoryEnum.PENDING);
        slotHistory = slotHistoryRepository.save(slotHistory);

        // create payment url
        long price = roomPricingRepository.findByTotalSlot(slot.getRoom().getTotalSlot()).getPrice();
        var payment = vNPayService.createPaymentUrl(slotHistory.getId(), createDate, price);

        // return url for frontend to redirect
        return new SlotBookingResponse(payment.getPaymentUrl());
    }

    @Transactional
    public PaymentResultResponse handlePaymentResult(HttpServletRequest request) {
        var result = vNPayService.handleResult(request);
        UUID slotHistoryId = result.getId();
        var slotHistory = slotHistoryRepository.findById(slotHistoryId).orElseThrow();
        if (slotHistory.getStatus().equals(StatusSlotHistoryEnum.PENDING) && result.getStatus().equals(VNPayStatus.SUCCESS)) {
            slotHistory.setStatus(StatusSlotHistoryEnum.SUCCESS);
            slotHistory = slotHistoryRepository.save(slotHistory);
        }
        if (slotHistory.getStatus().equals(StatusSlotHistoryEnum.PENDING) && result.getStatus() != VNPayStatus.SUCCESS) {
            slotHistory.setStatus(StatusSlotHistoryEnum.FAIL);
            slotHistoryRepository.save(slotHistory);
        }
        slotHistory = slotHistoryRepository.findByIdAndFetchDetails(slotHistoryId);

        return PaymentResultResponse.builder()
                .dormName(slotHistory.getSlot().getRoom().getDorm().getDormName())
                .floor(slotHistory.getSlot().getRoom().getFloor())
                .roomNumber(slotHistory.getSlot().getRoom().getRoomNumber())
                .slotName(slotHistory.getSlot().getSlotName())
                .result(result.getStatus())
                .build();
    }
}

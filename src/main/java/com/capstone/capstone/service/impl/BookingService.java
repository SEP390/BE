package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.dto.response.booking.PaymentResultResponse;
import com.capstone.capstone.dto.response.booking.SlotBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotHistoryResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotRepository slotRepository;
    private final SlotHistoryRepository slotHistoryRepository;
    private final VNPayService vNPayService;
    private final RoomPricingService roomPricingService;
    private final SlotHistoryService slotHistoryService;
    private final SlotService slotService;
    private final RoomService roomService;

    public SlotHistoryResponse getCurrentBooking(User user) {
        SlotHistory slotHistory = slotHistoryService.getCurrent(user);

        if (slotHistory == null) {
            return null;
        }

        // Pending booking
        if (slotHistory.getStatus() == StatusSlotHistoryEnum.PENDING) {
            // Expired 10 minutes
            if (ChronoUnit.MINUTES.between(slotHistory.getCreateDate(), LocalDateTime.now()) > 10) {
                slotHistory.setStatus(StatusSlotHistoryEnum.EXPIRE);
                slotHistory = slotHistoryRepository.save(slotHistory);
            }
        }

        // Fetch details
        slotHistory = slotHistoryService.getDetails(slotHistory);

        return SlotHistoryResponse.builder()
                .semesterId(slotHistory.getSemester().getId())
                .semesterName(slotHistory.getSemester().getName())
                .dormId(slotHistory.getSlot().getRoom().getDorm().getId())
                .dormName(slotHistory.getSlot().getRoom().getDorm().getDormName())
                .roomId(slotHistory.getSlot().getRoom().getId())
                .roomNumber(slotHistory.getSlot().getRoom().getRoomNumber())
                .floor(slotHistory.getSlot().getRoom().getFloor())
                .slotId(slotHistory.getSlot().getId())
                .slotName(slotHistory.getSlot().getSlotName())
                .createdDate(slotHistory.getCreateDate())
                .status(slotHistory.getStatus())
                .build();
    }

    @Transactional
    public SlotBookingResponse createBooking(User user, UUID slotId) {
        Slot slot = slotService.getById(slotId);

        // lock slot (so other user cannot book this slot)
        slotService.lock(slot);

        // check if room is full and update room status to full
        roomService.checkFullAndUpdate(slot.getRoom());

        // create slot history
        SlotHistory slotHistory = slotHistoryService.createNew(user, slot);

        // create payment url
        long price = roomPricingService.getPriceOfRoom(slot.getRoom());
        var payment = vNPayService.createPaymentUrl(slotHistory.getId(), slotHistory.getCreateDate(), price);

        // return url for frontend to redirect
        return new SlotBookingResponse(payment.getPaymentUrl());
    }

    @Transactional
    public PaymentResultResponse handlePaymentResult(HttpServletRequest request, User user) {
        var result = vNPayService.handleResult(request);

        var slotHistory = slotHistoryService.getById(result.getId());

        // unauthorize check
        if (!slotHistory.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // payment success
        if (slotHistory.getStatus() == StatusSlotHistoryEnum.PENDING && result.getStatus() == VNPayStatus.SUCCESS) {
            slotHistoryService.success(slotHistory);
        }

        // payment fail
        if (slotHistory.getStatus().equals(StatusSlotHistoryEnum.PENDING) && result.getStatus() != VNPayStatus.SUCCESS) {
            slotHistoryService.fail(slotHistory);
        }

        slotHistory = slotHistoryService.getDetails(slotHistory);

        return PaymentResultResponse.builder()
                .dormName(slotHistory.getSlot().getRoom().getDorm().getDormName())
                .floor(slotHistory.getSlot().getRoom().getFloor())
                .roomNumber(slotHistory.getSlot().getRoom().getRoomNumber())
                .slotName(slotHistory.getSlot().getSlotName())
                .result(result.getStatus())
                .build();
    }

    public List<SlotHistoryResponse> getHistory(UUID currentUserId) {
        List<SlotHistory> shs = slotHistoryRepository.findAllByUser(currentUserId);
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
                .status(sh.getStatus())
                .build()).toList();
    }
}

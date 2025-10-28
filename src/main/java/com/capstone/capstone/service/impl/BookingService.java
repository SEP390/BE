package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotService slotService;
    private final PaymentService paymentService;
    private final RoomService roomService;
    private final ModelMapper modelMapper;

    @Transactional
    public CreateBookingResponse create(CreateBookingRequest request) {
        // get current user
        User user = SecurityUtils.getCurrentUser();

        // get slot
        UUID slotId = request.getSlotId();
        Slot slot = slotService.getById(slotId).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));

        if (paymentService.hasBooking(user, slot)) throw new AppException("ALREADY_BOOKED");

        // create payment
        Payment payment = paymentService.create(user, slot);

        // create payment url for invoice
        String paymentUrl = paymentService.createPaymentUrl(payment);

        // lock slot (so other user cannot book this slot)
        roomService.lockSlot(slot, user);

        // return url for frontend to redirect
        return new CreateBookingResponse(paymentUrl);
    }

    public PagedModel<BookingHistoryResponse> history(List<PaymentStatus> status, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        return paymentService.getBookingHistory(user, status, pageable);
    }

    @Transactional
    public SlotResponseJoinRoomAndDormAndPricing current() {
        User user = SecurityUtils.getCurrentUser();
        Slot slot = slotService.getByUser(user);
        if (slot == null) return null;
        Payment payment = paymentService.getLatestPendingBookingByUserAndSlot(user, slot);
        if (payment != null) {
            // unlock if expire
            if (paymentService.isExpire(payment) && slot.getStatus() == StatusSlotEnum.LOCK) {
                payment = paymentService.expire(payment);
                slot = slotService.unlock(slot);
                return null;
            }
        } else {
            // bug: no payment but lock slot
            if (slot.getStatus() == StatusSlotEnum.LOCK) slot = slotService.unlock(slot);
        }
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricing.class);
    }

    @Transactional
    public String getLatestPendingUrl() {
        User user = SecurityUtils.getCurrentUser();
        Payment payment = paymentService.getLatestPendingBookingByUser(user);
        if (payment == null) throw new AppException("PAYMENT_NOT_FOUND");
        return paymentService.createPaymentUrl(payment);
    }
}

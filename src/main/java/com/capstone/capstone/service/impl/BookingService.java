package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotService slotService;
    private final PaymentService paymentService;
    private final SlotHistoryService slotHistoryService;
    private final UserRepository userRepository;

    @Transactional
    public CreateBookingResponse create(CreateBookingRequest request) {
        // get current user
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));

        // get slot
        UUID slotId = request.getSlotId();
        Slot slot = slotService.getById(slotId);

        // slot not found
        if (slot == null) {
            throw new AppException("SLOT_NOT_FOUND");
        }

        // create slot history
        SlotHistory history = slotHistoryService.create(user, slot);

        // create payment
        Payment payment = paymentService.createForSlot(user, history);

        // create payment url for invoice
        String paymentUrl = paymentService.createPaymentUrl(payment);

        // lock slot (so other user cannot book this slot)
        slotService.lock(slot, user);

        // return url for frontend to redirect
        return new CreateBookingResponse(paymentUrl);
    }

    public Page<BookingHistoryResponse> history(PaymentStatus status, int page) {
        User user = new User();
        user.setId(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        return paymentService.bookingHistory(user, status, page);
    }

    public BookingHistoryResponse current() {
        return paymentService.currentBooking();
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.dto.response.booking.CurrentSlotResponse;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotService slotService;
    private final PaymentService paymentService;
    private final SlotHistoryService slotHistoryService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoomPricingService roomPricingService;
    private final RoomSlotService roomSlotService;

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
        Payment payment = paymentService.create(history);

        // create payment url for invoice
        String paymentUrl = paymentService.createPaymentUrl(payment);

        // lock slot (so other user cannot book this slot)
        roomSlotService.lockSlot(slot, user);

        // return url for frontend to redirect
        return new CreateBookingResponse(paymentUrl);
    }

    public PagedModel<BookingHistoryResponse> history(List<PaymentStatus> status, Pageable pageable) {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        return paymentService.getBookingHistory(user, status, pageable);
    }

    @Transactional
    public CurrentSlotResponse current() {
        User user = userRepository.getReferenceById(Objects.requireNonNull(AuthenUtil.getCurrentUserId()));
        Slot currentSlot = slotService.getByUser(user);
        if (currentSlot == null) return null;
        var res = modelMapper.map(currentSlot, CurrentSlotResponse.class);
        res.setPrice(roomPricingService.getPriceOfSlot(currentSlot));
        return res;
    }
}

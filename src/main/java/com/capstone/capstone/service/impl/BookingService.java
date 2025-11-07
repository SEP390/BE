package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.booking.BookingHistoryResponse;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotService slotService;
    private final PaymentSlotService paymentSlotService;
    private final RoomService roomService;
    private final SlotRepository slotRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public CreateBookingResponse create(CreateBookingRequest request) {
        // get current user
        User user = SecurityUtils.getCurrentUser();

        // get slot
        Slot slot = slotRepository.findById(request.getSlotId()).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));

        // slot not available
        if (slot.getStatus() != StatusSlotEnum.AVAILABLE) throw new AppException("SLOT_NOT_AVAILABLE");

        // already book other slot
        if (roomService.getSlotByUser(user).isPresent()) throw new AppException("ALREADY_BOOKED");

        // create payment url
        String paymentUrl = paymentSlotService.createPaymentUrl(user, slot);

        // lock slot (so other user cannot book this slot)
        roomService.lockSlot(slot, user);

        // return url for frontend to redirect
        return new CreateBookingResponse(paymentUrl);
    }

    public PagedModel<BookingHistoryResponse> history(List<PaymentStatus> status, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        return paymentSlotService.getHistory(user, status, pageable);
    }

    /**
     * Get current slot
     *
     * @return slot
     */
    @Transactional
    public SlotResponseJoinRoomAndDormAndPricing current() {
        User user = SecurityUtils.getCurrentUser();
        Slot slot = slotService.getByUser(user).orElse(null);
        if (slot == null) return null;
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricing.class);
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.dto.response.booking.PaymentResultResponse;
import com.capstone.capstone.dto.response.booking.SlotBookingResponse;
import com.capstone.capstone.dto.response.booking.SlotHistoryResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Example;
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
    private final UserRepository userRepository;
    private final SlotHistoryRepository slotHistoryRepository;
    private final VNPayService vNPayService;
    private final RoomPricingRepository roomPricingRepository;
    private final SemesterRepository semesterRepository;
    private final RoomRepository roomRepository;

    public SlotHistoryResponse getCurrentBooking(UUID currentUserId) {
        var nextSemester = semesterRepository.findNextSemester();
        SlotHistory slotHistory = slotHistoryRepository.findCurrentSlotHistory(currentUserId, nextSemester.getId());

        if (slotHistory == null) {
            return null;
        }
        if (slotHistory.getStatus() == StatusSlotHistoryEnum.PENDING && ChronoUnit.MINUTES.between(slotHistory.getCreateDate(), LocalDateTime.now()) > 10) {
            slotHistory.setStatus(StatusSlotHistoryEnum.EXPIRE);
            slotHistory = slotHistoryRepository.save(slotHistory);
        }
        slotHistory = slotHistoryRepository.findByIdAndFetchDetails(slotHistory.getId());

        return SlotHistoryResponse.builder()
                .semesterId(nextSemester.getId())
                .semesterName(nextSemester.getName())
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
    public SlotBookingResponse createBooking(UUID currentUserId, UUID slotId) {
        Slot slot = slotRepository.findById(slotId).orElseThrow();
        Semester nextSemester = semesterRepository.findNextSemester();

        // lock slot (so other user cannot book this slot)
        if (slot.getStatus() == StatusSlotEnum.UNAVAILABLE) throw new RuntimeException("Slot is unavailable");
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);

        // set user to slot
        User user = userRepository.getReferenceById(currentUserId);
        slot.setUser(user);
        slot = slotRepository.save(slot);

        Slot example = new Slot();
        example.setRoom(slot.getRoom());
        List<Slot> slots = slotRepository.findAll(Example.of(example));
        if (slots.stream().allMatch(s -> s.getStatus().equals(StatusSlotEnum.UNAVAILABLE))) {
            Room room = roomRepository.findById(slot.getRoom().getId()).orElseThrow();
            room.setStatus(StatusRoomEnum.FULL);
            roomRepository.save(room);
        }

        // create history
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setSlot(slot);
        var createDate = LocalDateTime.now();
        slotHistory.setCreateDate(createDate);
        slotHistory.setSemester(nextSemester);


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

        // unlock slot
        Slot slot = slotHistory.getSlot();
        slot = slotRepository.findById(slot.getId()).orElseThrow();
        if (slotHistory.getStatus() != StatusSlotHistoryEnum.SUCCESS) {
            slot.setStatus(StatusSlotEnum.AVAILABLE);
        }

        return PaymentResultResponse.builder()
                .dormName(slotHistory.getSlot().getRoom().getDorm().getDormName())
                .floor(slotHistory.getSlot().getRoom().getFloor())
                .roomNumber(slotHistory.getSlot().getRoom().getRoomNumber())
                .slotName(slotHistory.getSlot().getSlotName())
                .result(result.getStatus())
                .build();
    }
}

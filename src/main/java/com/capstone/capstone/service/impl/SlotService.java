package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.checkin.GuardCheckinRequest;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final SlotHistoryRepository slotHistoryRepository;
    private final SlotInvoiceRepository slotInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public Slot save(Slot slot) {
        return slotRepository.save(slot);
    }

    /**
     * Get slot by current user in it
     *
     * @param user user
     * @return slot
     */
    public Optional<Slot> getByUser(User user) {
        return Optional.ofNullable(slotRepository.findByUser(user));
    }

    /**
     * Get slot by current user in it
     */
    @Transactional
    public SlotResponseJoinRoomAndDormAndPricing getCurrent() {
        User user = SecurityUtils.getCurrentUser();
        Slot slot = getByUser(user).orElse(null);
        if (slot == null) return null;
        final UUID slotId = slot.getId();
        if (slot.getStatus() == StatusSlotEnum.LOCK) {
            Invoice invoice = invoiceRepository.findLatestBookingInvoice(user).orElse(null);
            Payment payment = paymentRepository.findLatestByInvoice(invoice).orElse(null);
            // ko có payment
            if (payment == null || invoice == null) {
                slot.setStatus(StatusSlotEnum.AVAILABLE);
                slot.setUser(null);
                slot = slotRepository.save(slot);
                return null;
            } else {
                // có payment hoặc invoice những đã hủy
                if (payment.getStatus() == PaymentStatus.CANCEL || payment.getInvoice().getStatus() == PaymentStatus.CANCEL) {
                    slot.setStatus(StatusSlotEnum.AVAILABLE);
                    slot.setUser(null);
                    slot = slotRepository.save(slot);
                    return null;
                }
                // invoice pending nhưng hết hạn
                if (payment.getInvoice().getStatus() == PaymentStatus.PENDING
                        && payment.getInvoice().getExpireTime().isBefore(LocalDateTime.now())) {
                    payment.setStatus(PaymentStatus.CANCEL);
                    payment = paymentRepository.save(payment);
                    invoice.setStatus(PaymentStatus.CANCEL);
                    invoice = invoiceRepository.save(invoice);
                    slot.setStatus(StatusSlotEnum.AVAILABLE);
                    slot.setUser(null);
                    slot = slotRepository.save(slot);
                    return null;
                }
            }
        }
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricing.class);
    }

    /**
     * Create slots for rooms (by totalSlot)
     *
     * @param room room
     * @return list slots
     */
    public List<Slot> create(Room room) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 1; i <= room.getTotalSlot(); i++) {
            Slot slot = new Slot();
            slot.setRoom(room);
            slot.setSlotName("%s_%s".formatted(room.getRoomNumber(), i));
            slot.setStatus(StatusSlotEnum.AVAILABLE);
            slots.add(slot);
        }
        return slotRepository.saveAll(slots);
    }

    /**
     * Delete all slots in room
     *
     * @param room room
     */
    public void deleteByRoom(Room room) {
        // TODO: check room contains users
        slotRepository.deleteAllByRoom(room);
    }

    public List<Slot> getByRoom(Room room) {
        return slotRepository.findByRoom(room);
    }

    public SlotResponseJoinRoomAndDormAndPricingAndUser getResponseById(UUID id) {
        Slot slot = slotRepository.findById(id).orElseThrow();
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricingAndUser.class);
    }

    /**
     * Danh sách các slot đang chờ checkin
     *
     * @param userCode mã sinh viên
     * @param pageable page, size
     * @return
     */
    public PagedModel<SlotResponseJoinRoomAndDormAndPricingAndUser> getAll(Map<String, Object> filter, Pageable pageable) {
        SpecQuery<Slot> query = new SpecQuery<>();
        query.like("userCode", (String) filter.get("userCode"));
        query.equal("userId", filter.get("userId"));
        query.equal("status", filter.get("status"));
        return new PagedModel<>(slotRepository.findAll(query.and(), pageable).map(s -> modelMapper.map(s, SlotResponseJoinRoomAndDormAndPricingAndUser.class)));
    }

    /**
     * [Guard] checkin slot cho sinh viên
     */
    public SlotResponseJoinRoomAndDormAndPricingAndUser checkin(GuardCheckinRequest request) {
        Slot slot = slotRepository.findById(request.getSlotId()).orElseThrow();
        User user = Optional.ofNullable(slot.getUser()).orElseThrow();
        // chuyển trạng thái sang unavailable
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        slot = slotRepository.save(slot);
        var his = slotHistoryRepository.findCurrent(user, slot.getId()).orElse(null);
        if (his != null) {
            his.setCheckin(LocalDateTime.now());
            his = slotHistoryRepository.save(his);
        }
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricingAndUser.class);
    }

    public void lock(Slot slot, User user) {
        slot.setStatus(StatusSlotEnum.LOCK);
        slot.setUser(user);
        slot = slotRepository.save(slot);
        if (roomRepository.isFull(slot.getRoom())) {
            Room room = slot.getRoom();
            room.setStatus(StatusRoomEnum.FULL);
            roomRepository.save(room);
        }
    }

    public void unlock(Slot slot) {
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slot.setUser(null);
        slot = slotRepository.save(slot);
        if (!roomRepository.isFull(slot.getRoom())) {
            Room room = slot.getRoom();
            room.setStatus(StatusRoomEnum.AVAILABLE);
            roomRepository.save(room);
        }
    }

    public void book(Slot slot, Semester semester) {
        slot.setStatus(StatusSlotEnum.CHECKIN);
        SlotHistory slotHistory = new SlotHistory();
        slotRepository.save(slot);
        slotHistory.setSlotId(slot.getId());
        slotHistory.setSlotName(slot.getSlotName());
        slotHistory.setRoom(slot.getRoom());
        slotHistory.setUser(slot.getUser());
        slotHistory.setSemester(semester);
        slotHistoryRepository.save(slotHistory);
    }

    public SlotResponseJoinRoomAndDormAndPricingAndUser checkout(UUID id) {
        Slot slot = slotRepository.findById(id).orElseThrow();
        User user = Optional.ofNullable(slot.getUser()).orElseThrow();
        // chuyển trạng thái sang available
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        // xóa user
        slot.setUser(null);
        slot = slotRepository.save(slot);
        var his = slotHistoryRepository.findCurrent(user, slot.getId()).orElse(null);
        if (his != null) {
            his.setCheckout(LocalDateTime.now());
            his = slotHistoryRepository.save(his);
        }
        return modelMapper.map(slot, SlotResponseJoinRoomAndDormAndPricingAndUser.class);
    }
}

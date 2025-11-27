package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusRoomEnum;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.checkin.GuardCheckinRequest;
import com.capstone.capstone.dto.request.slot.SwapSlotRequest;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.room.RoomResponseJoinPricing;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDorm;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricing;
import com.capstone.capstone.dto.response.slot.SlotResponseJoinRoomAndDormAndPricingAndUser;
import com.capstone.capstone.dto.response.slot.SwapSlotResponse;
import com.capstone.capstone.dto.response.slotHistory.SlotHistoryResponse;
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
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final SemesterService semesterService;
    private final InvoiceChangeService invoiceChangeService;

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
                    invoiceChangeService.onChange(invoice);
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
     */
    public PagedModel<SlotResponseJoinRoomAndDormAndPricingAndUser> getAll(Map<String, Object> filter, Pageable pageable) {
        SpecQuery<Slot> query = new SpecQuery<>();
        query.like("userCode", (String) filter.get("userCode"));
        query.equal("userId", filter.get("userId"));
        query.equal("status", filter.get("status"));
        query.equal(filter, r -> r.get("room").get("id"), "roomId");
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

    public SlotResponseJoinRoomAndDormAndPricingAndUser checkout(UUID id) {
        Slot slot = slotRepository.findById(id).orElseThrow();
        User user = Optional.ofNullable(slot.getUser()).orElseThrow(() -> new AppException("SLOT_EMPTY"));
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

    @Transactional
    public SwapSlotResponse swap(SwapSlotRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new AppException("USER_NOT_FOUND"));
        Slot currentSlot = Optional.ofNullable(user.getSlot()).orElseThrow(() -> new AppException("CURRENT_SLOT_NOT_FOUND"));
        Slot newSlot = slotRepository.findById(request.getSlotId()).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));
        RoomPricing currentPricing = newSlot.getRoom().getPricing();
        RoomPricing newPricing = currentSlot.getRoom().getPricing();
        if (newPricing.getTotalSlot() < currentPricing.getTotalSlot())
            throw new AppException("SLOT_LOWER_PRICE");
        if (currentSlot.getId().equals(newSlot.getId())) throw new AppException("SAME_SLOT");

        if(!roomRepository.isValid(newSlot.getRoom(), user.getGender())) throw new AppException("GENDER_INVALID");
        SwapSlotResponse response = new SwapSlotResponse();
        if (newPricing.getTotalSlot() > currentPricing.getTotalSlot()) {
            Invoice invoice = new Invoice();
            invoice.setStatus(PaymentStatus.PENDING);
            invoice.setUser(user);
            invoice.setType(InvoiceType.SWAP);
            invoice.setReason("Tiền đổi phòng %s sang %s".formatted(currentSlot.getRoom().getRoomNumber(), newSlot.getRoom().getRoomNumber()));
            invoice.setCreateTime(LocalDateTime.now());
            invoice = invoiceRepository.save(invoice);
            response.setInvoice(modelMapper.map(invoice, InvoiceResponse.class));
        }
        Semester semester = semesterService.getCurrent().orElseThrow(() -> new AppException("SEMESTER_NOT_FOUND"));
        currentSlot.setUser(null);
        currentSlot.setStatus(StatusSlotEnum.AVAILABLE);
        currentSlot = slotRepository.save(currentSlot);
        var his = slotHistoryRepository.findCurrent(user, currentSlot.getId()).orElse(null);
        if (his != null) {
            his.setCheckout(LocalDateTime.now());
            his = slotHistoryRepository.save(his);
        }
        newSlot.setUser(user);
        newSlot.setStatus(StatusSlotEnum.UNAVAILABLE);
        newSlot = slotRepository.save(newSlot);
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setCheckin(LocalDateTime.now());
        slotHistory.setSlotId(newSlot.getId());
        slotHistory.setSlotName(newSlot.getSlotName());
        slotHistory.setRoom(newSlot.getRoom());
        slotHistory.setUser(user);
        slotHistory.setSemester(semester);
        slotHistory = slotHistoryRepository.save(slotHistory);
        response.setOldSlot(modelMapper.map(currentSlot, SlotResponseJoinRoomAndDorm.class));
        response.setNewSlot(modelMapper.map(newSlot, SlotResponseJoinRoomAndDorm.class));
        response.setSlotHistory(modelMapper.map(slotHistory, SlotHistoryResponse.class));
        return response;
    }

    @Transactional
    public Integer getSwapCount(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("USER_NOT_FOUND"));
        if (user.getSlot() == null) throw new AppException("SLOT_NOT_FOUND");
        return roomRepository.findSwapableCount(user.getGender(), user.getSlot().getRoom().getTotalSlot());
    }

    @Transactional
    public Map<String, Object> getSwapDetail(UUID userId, UUID roomId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("USER_NOT_FOUND"));
        Room room = roomId == null ? null : roomRepository.findById(roomId).orElse(null);
        Map<String, Object> res = new HashMap<>();
        res.put("old", user.getSlot() == null ? null : modelMapper.map(user.getSlot().getRoom(), RoomResponseJoinPricing.class));
        res.put("new", room == null ? null : modelMapper.map(room, RoomResponseJoinPricing.class));
        if (user.getSlot() == null || room == null) {
            res.put("swapable", false);
            return res;
        }
        if (room.getStatus() != StatusRoomEnum.AVAILABLE) {
            res.put("swapable", false);
        } else res.put("swapable", roomRepository.isValid(room, user.getGender()));
        return res;
    }
}

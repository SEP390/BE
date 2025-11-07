package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.RoomRepository;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.repository.SlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;
    private final SemesterService semesterService;
    private final RoomPricingService roomPricingService;
    private final RoomRepository roomRepository;
    private final SlotRepository slotRepository;

    public boolean has(User user, Semester semester) {
        return slotHistoryRepository.exists((r, q, c) -> {
            return c.and(
                    c.equal(r.get("user"), user),
                    c.equal(r.get("semester"), semester),
                    c.isNotNull(r.get("slotId"))
            );
        });
    }

    public List<Room> getRooms(User user, Semester semester) {
        return slotHistoryRepository.findAll((r, q, c) -> {
            return c.and(
                    c.equal(r.get("user"), user),
                    c.equal(r.get("semester"), semester),
                    c.isNotNull(r.get("slotId"))
            );
        }).stream().map(sh -> roomRepository.findById(sh.getRoomId()).orElse(null)).toList();
    }

    public List<Slot> getSlots(User user, Semester semester) {
        return slotHistoryRepository.findAll((r, q, c) -> {
            return c.and(
                    c.equal(r.get("user"), user),
                    c.equal(r.get("semester"), semester),
                    c.isNotNull(r.get("slotId"))
            );
        }).stream().map(sh -> slotRepository.findById(sh.getSlotId()).orElse(null)).toList();
    }

    /**
     * Create slot history for user in next semester
     *
     * @param user user
     * @param slot slot
     * @return history
     */
    public SlotHistory create(User user, Slot slot) {
        // get next semester
        Semester semester = semesterService.getNext();
        if (semester == null) throw new AppException("NEXT_SEMESTER_NOT_FOUND");
        return create(user, semester, slot);
    }

    public SlotHistory create(User user, Semester semester, Slot slot) {
        // get current price of slot (can be updated in the future, so only save price in slot history)
        var pricing = roomPricingService.getBySlot(slot).orElseThrow(() -> new AppException("INVALID_ROOM_TYPE"));

        return slotHistoryRepository.save(
                SlotHistory.builder()
                        .slotId(slot.getId())
                        .slotName(slot.getSlotName())
                        .roomId(slot.getRoom().getId())
                        .roomNumber(slot.getRoom().getRoomNumber())
                        .dormName(slot.getRoom().getDorm().getDormName())
                        .user(user)
                        .semester(semester)
                        .price(pricing.getPrice())
                        .build()
        );
    }

    public SlotHistory create(User user, Semester semester, Slot slotFrom, Slot slotTo) {
        // get current price of slot (can be updated in the future, so only save price in slot history)
        var pricing = roomPricingService.getBySlot(slotTo).orElseThrow(() -> new AppException("INVALID_ROOM_TYPE"));

        return slotHistoryRepository.save(
                SlotHistory.builder()
                        .fromSlotId(slotFrom.getId())
                        .fromRoomId(slotFrom.getRoom().getId())
                        .slotId(slotTo.getId())
                        .slotName(slotTo.getSlotName())
                        .roomId(slotTo.getRoom().getId())
                        .roomNumber(slotTo.getRoom().getRoomNumber())
                        .dormName(slotTo.getRoom().getDorm().getDormName())
                        .user(user)
                        .semester(semester)
                        .price(pricing.getPrice())
                        .build()
        );
    }
}

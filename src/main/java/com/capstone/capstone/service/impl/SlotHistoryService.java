package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SlotHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;
    private final SemesterService semesterService;
    private final RoomPricingService roomPricingService;

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
        return create(user, semester, slot);
    }

    public SlotHistory create(User user, Semester semester, Slot slot) {
        // get current price of slot (can be updated in future)
        var pricing = roomPricingService.getBySlot(slot).orElse(null);
        if (pricing == null) throw new AppException("INVALID_ROOM_TYPE");

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
                        .checkin(LocalDate.now())
                        .build()
        );
    }
}

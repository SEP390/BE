package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SlotHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;
    private final SemesterService semesterService;
    private final RoomPricingService roomPricingService;

    /**
     * Create slot history for user in next semester
     * @param user user
     * @param slot slot
     * @return history
     */
    public SlotHistory create(User user, Slot slot) {
        // get next semester
        Semester semester = semesterService.getNext();

        LocalDateTime createDate = LocalDateTime.now();

        // get current price of slot (can be updated in future)
        var pricing = roomPricingService.getBySlot(slot).orElse(null);
        if (pricing == null) throw new AppException("INVALID_ROOM_TYPE");

        // create slot history
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setSlot(slot);
        slotHistory.setUser(user);
        slotHistory.setPrice(pricing.getPrice());
        slotHistory.setSlot(slot);
        slotHistory.setSemester(semester);
        slotHistory = slotHistoryRepository.save(slotHistory);

        return slotHistory;
    }
}

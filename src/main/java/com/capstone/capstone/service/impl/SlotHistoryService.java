package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.SlotHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;

    private final SemesterService semesterService;
    private final SlotService slotService;

    public SlotHistory getCurrent(User user) {
        var semester = semesterService.getNextSemester();
        return slotHistoryRepository.findCurrentSlotHistory(user, semester);
    }

    public SlotHistory getDetails(SlotHistory slotHistory) {
        return slotHistoryRepository.findDetails(slotHistory);
    }

    public SlotHistory createNew(User user, Slot slot) {
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setSlot(slot);
        var createDate = LocalDateTime.now();
        slotHistory.setCreateDate(createDate);
        slotHistory.setSemester(semesterService.getNextSemester());

        slotHistory.setUser(user);
        slotHistory.setStatus(StatusSlotHistoryEnum.PENDING);
        slotHistory = slotHistoryRepository.save(slotHistory);
        return slotHistory;
    }

    public SlotHistory getById(UUID id) {
        return slotHistoryRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void success(SlotHistory slotHistory) {
        slotService.setUser(slotHistory.getSlot(), slotHistory.getUser());
        slotHistory.setStatus(StatusSlotHistoryEnum.SUCCESS);
        slotHistoryRepository.save(slotHistory);
    }

    @Transactional
    public void fail(SlotHistory slotHistory) {
        slotService.unlock(slotHistory.getSlot());
        slotHistory.setStatus(StatusSlotHistoryEnum.FAIL);
        slotHistoryRepository.save(slotHistory);
    }
}

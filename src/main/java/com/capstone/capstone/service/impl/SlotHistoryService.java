package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.dto.response.booking.SlotHistoryResponse;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.repository.SlotHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SlotHistoryService {
    private final SlotHistoryRepository slotHistoryRepository;

    public SlotHistoryResponse getCurrentSemesterSlotHistory(UUID currentUserId) {
        SlotHistory slotHistory = slotHistoryRepository.findCurrentSemesterSlotHistory(currentUserId);
        if (ChronoUnit.MINUTES.between(slotHistory.getCreateDate(), LocalDateTime.now()) > 10) {
            slotHistory.setStatus(StatusSlotHistoryEnum.EXPIRE);
            slotHistory = slotHistoryRepository.save(slotHistory);
        }
        return SlotHistoryResponse.builder()
                .slotId(slotHistory.getSlot().getId())
                .createdDate(slotHistory.getCreateDate())
                .status(slotHistory.getStatus())
                .build();
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotHistoryEnum;
import com.capstone.capstone.entity.Invoice;
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

    public SlotHistory create(User user, Slot slot, Invoice invoice) {
        SlotHistory slotHistory = new SlotHistory();
        slotHistory.setSlot(slotService.findByInvoice(invoice));
        var createDate = LocalDateTime.now();
        slotHistory.setCreateDate(createDate);
        slotHistory.setInvoice(invoice);
        slotHistory.setUser(user);
        slotHistory.setSlot(slot);
        slotHistory.setSemester(semesterService.getNextSemester());

        slotHistory.setUser(invoice.getUser());
        slotHistory = slotHistoryRepository.save(slotHistory);
        return slotHistory;
    }

    public SlotHistory getById(UUID id) {
        return slotHistoryRepository.findById(id).orElseThrow();
    }

    public SlotHistory findByInvoice(Invoice invoice) {
        return slotHistoryRepository.findByInvoice(invoice);
    }
}

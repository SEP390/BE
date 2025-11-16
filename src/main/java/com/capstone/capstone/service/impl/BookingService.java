package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.SurveySelectRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotRepository slotRepository;
    private final SurveySelectRepository surveySelectRepository;
    private final RoomService roomService;
    private final SlotService slotService;
    private final SlotInvoiceService slotInvoiceService;
    private final SemesterService semesterService;

    @Transactional
    public String create(UUID slotId) {
        // get current user
        User user = SecurityUtils.getCurrentUser();

        // next semester
        Semester nextSemester = semesterService.getNext();

        if (nextSemester == null) throw new AppException("NEXT_SEMESTER_NOT_FOUND");

        // không được đặt phòng nếu chưa làm survey
        if (!surveySelectRepository.exists((r, q, c) -> {
            return c.equal(r.get("user"), user);
        })) throw new AppException("NO_SURVEY");

        // đã đặt slot khác
        if (slotService.getByUser(user).isPresent()) throw new AppException("ALREADY_BOOKED");

        // slot không tồn tại
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));

        // slot not available
        if (slot.getStatus() != StatusSlotEnum.AVAILABLE) throw new AppException("SLOT_NOT_AVAILABLE");

        // tạo url thanh toán
        String paymentUrl = slotInvoiceService.createPaymentUrl(user, slot, nextSemester);

        // khóa slot
        slotService.lock(slot, user);

        // return url for frontend to redirect
        return paymentUrl;
    }
}

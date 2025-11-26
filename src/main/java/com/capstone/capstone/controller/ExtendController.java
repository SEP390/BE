package com.capstone.capstone.controller;

import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.SemesterRepository;
import com.capstone.capstone.repository.SlotHistoryRepository;
import com.capstone.capstone.service.impl.*;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@AllArgsConstructor
public class ExtendController {
    private final TimeConfigService timeConfigService;
    private final SlotHistoryRepository slotHistoryRepository;
    private final SemesterRepository semesterRepository;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final SlotService slotService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    @GetMapping("/api/booking/extend")
    public BaseResponse<String> extend() {
        User user = SecurityUtils.getCurrentUser();
        TimeConfig timeConfig = timeConfigService.getCurrent().orElseThrow(() -> new AppException("TIME_CONFIG_NOT_FOUND"));
        // chưa từng đặt phòng
        if (!slotHistoryRepository.existsByUser(user)) throw new AppException("SLOT_HISTORY_NOT_FOUND");
        Semester nextSemester = semesterRepository.findNextSemester();
        if (nextSemester == null) throw new AppException("NEXT_SEMESTER_NOT_FOUND");
        SlotHistory slotHistory = slotHistoryRepository.findCurrent(user).orElse(null);
        if (slotHistory != null) {
            if (slotHistory.getSemester().getId().equals(nextSemester.getId())) {
                throw new AppException("ALREADY_BOOK");
            }
            slotHistory.setCheckout(LocalDateTime.now());
            slotHistoryRepository.save(slotHistory);
        }
        LocalDate today = LocalDate.now();
        if (!(today.isBefore(timeConfig.getEndExtendDate()) && today.isAfter(timeConfig.getStartExtendDate())))
            throw new AppException("EXTEND_DATE_NOT_START", List.of(timeConfig.getEndExtendDate(), timeConfig.getStartExtendDate()));
        Slot slot = user.getSlot();
        if (slot == null) throw new AppException("SLOT_NOT_FOUND");
        slotService.lock(slot, user);
        Invoice invoice = invoiceService.create(user, slot, nextSemester);
        // tạo lần thanh toán
        Payment payment = paymentService.create(invoice);
        String paymentUrl = paymentService.getPaymentUrl(payment);

        // hủy hóa đơn sau 10 phút
        scheduledExecutorService.schedule(new InvoiceExpireService(invoiceRepository, paymentRepository, invoice), 10, TimeUnit.MINUTES);

        return new BaseResponse<>(paymentUrl);
    }
}

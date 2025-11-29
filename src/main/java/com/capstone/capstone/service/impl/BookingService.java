package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class BookingService {
    private final SlotRepository slotRepository;
    private final SlotService slotService;
    private final InvoiceService invoiceService;
    private final SemesterService semesterService;
    private final PaymentService paymentService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SlotInvoiceRepository slotInvoiceRepository;
    private final VNPayService vnPayService;
    private final RoomRepository roomRepository;
    private final BookingValidateService bookingValidateService;

    @Transactional
    public String create(UUID slotId) {
        // get current user
        User user = SecurityUtils.getCurrentUser();

        // next semester
        Semester nextSemester = semesterService.getNext();

        if (nextSemester == null) throw new AppException("NEXT_SEMESTER_NOT_FOUND");

        bookingValidateService.validate();

        // đã đặt slot khác
        if (slotService.getByUser(user).isPresent()) throw new AppException("ALREADY_BOOKED");

        // slot không tồn tại
        Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));

        // slot not available
        if (slot.getStatus() != StatusSlotEnum.AVAILABLE) throw new AppException("SLOT_NOT_AVAILABLE");

        if (!roomRepository.isValid(slot.getRoom(), user.getGender())) throw new AppException("GENDER_INVALID");

        // tạo hóa đơn
        Invoice invoice = invoiceService.create(user, slot, nextSemester);

        // tạo lần thanh toán
        Payment payment = paymentService.create(invoice);

        String paymentUrl = paymentService.getPaymentUrl(payment);

        // khóa slot
        slotService.lock(slot, user);

        // hủy hóa đơn sau 10 phút
        scheduledExecutorService.schedule(new InvoiceExpireService(invoiceRepository, paymentRepository, invoice), 10, TimeUnit.MINUTES);

        // return url for frontend to redirect
        return paymentUrl;
    }

    @Transactional
    public String cancel() {
        User user = SecurityUtils.getCurrentUser();
        Slot slot = slotService.getByUser(user).orElseThrow();
        final UUID slotId = slot.getId();
        if (slot.getStatus() == StatusSlotEnum.LOCK) {
            Payment payment = paymentRepository.findOne((r, q, c) -> {
                return c.equal(r.get("invoice").get("slotInvoice").get("slotId"), slotId);
            }).orElse(null);
            if (payment == null) {
                slot.setStatus(StatusSlotEnum.AVAILABLE);
                slot.setUser(null);
                slot = slotRepository.save(slot);
            } else {
                if (payment.getInvoice().getStatus() == PaymentStatus.PENDING) {
                    throw new AppException("PAYMENT_PENDING");
                } else {
                    Invoice invoice = payment.getInvoice();
                    invoice.setStatus(PaymentStatus.CANCEL);
                    invoice = invoiceRepository.save(invoice);
                    slot.setStatus(StatusSlotEnum.AVAILABLE);
                    slot.setUser(null);
                    slot = slotRepository.save(slot);
                }
            }
        } else throw new AppException("BOOKING_NOT_FOUND");
        return "SUCCESS";
    }

    public String payment() {
        User user = SecurityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findLatestBookingInvoice(user).orElseThrow(() -> new AppException("INVOICE_NOT_FOUND"));
        if (invoice.getStatus() == PaymentStatus.SUCCESS) {
            throw new AppException("INVOICE_ALREADY_PAID");
        }
        if (invoice.getStatus() == PaymentStatus.CANCEL) {
            throw new AppException("INVOICE_CANCEL");
        }
        var payment = paymentRepository.findLatestByInvoice(invoice).orElseThrow(() -> new AppException("PAYMENT_NOT_FOUND"));
        // hết hạn thanh toán
        if (invoice.getExpireTime().isBefore(LocalDateTime.now())) {
            invoice.setStatus(PaymentStatus.CANCEL);
            invoice = invoiceRepository.save(invoice);
            payment.setStatus(PaymentStatus.CANCEL);
            payment = paymentRepository.save(payment);
            SlotInvoice slotInvoice = slotInvoiceRepository.findById(invoice.getSlotInvoice().getId()).orElseThrow();
            Slot slot = slotRepository.findById(slotInvoice.getSlotId()).orElseThrow();
            slot.setUser(null);
            slot.setStatus(StatusSlotEnum.AVAILABLE);
            slot = slotRepository.save(slot);
            throw new AppException("INVOICE_EXPIRED");
        }
        return vnPayService.createPaymentUrl(payment.getId(), payment.getCreateTime(), payment.getPrice());
    }
}

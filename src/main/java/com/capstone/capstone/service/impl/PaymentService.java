package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.PaymentResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final VNPayService vnPayService;
    private final SlotService slotService;
    private final InvoiceRepository invoiceRepository;
    private final SlotRepository slotRepository;
    private final SlotInvoiceRepository slotInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SlotHistoryRepository slotHistoryRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    /**
     * Thanh toán
     *
     * @param request request
     * @return
     */
    @Transactional
    public PaymentResponse handle(HttpServletRequest request) {
        var res = vnPayService.verify(request);
        var paymentId = res.getId();
        var payment = paymentRepository.findById(paymentId).orElseThrow();
        var invoice = payment.getInvoice();
        var vnPayStatus = res.getStatus();
        // chỉ cập nhật nếu đang PENDING (double render problem)
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            if (vnPayStatus == VNPayStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment = paymentRepository.save(payment);
                invoice.setStatus(PaymentStatus.SUCCESS);
                invoice = invoiceRepository.save(invoice);
                // đặt phòng thành công
                if (invoice.getType() == InvoiceType.BOOKING) {
                    UUID slotId = invoice.getSlotInvoice().getSlotId();
                    Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));
                    // chuyển slot sang trạng thái checkin
                    slot.setStatus(StatusSlotEnum.CHECKIN);
                    // tạo lịch sử
                    SlotHistory slotHistory = new SlotHistory();
                    slotRepository.save(slot);
                    slotHistory.setSlotId(slot.getId());
                    slotHistory.setSlotName(slot.getSlotName());
                    slotHistory.setRoom(slot.getRoom());
                    slotHistory.setUser(slot.getUser());
                    slotHistory.setSemester(invoice.getSlotInvoice().getSemester());
                    slotHistoryRepository.save(slotHistory);
                }
            } else {
                payment.setStatus(PaymentStatus.CANCEL);
                payment = paymentRepository.save(payment);
                // hủy thanh toán đặt phòng
                if (invoice.getType() == InvoiceType.BOOKING) {
                    invoice.setStatus(PaymentStatus.CANCEL);
                    invoice = invoiceRepository.save(invoice);
                    Slot slot = slotRepository.findById(invoice.getSlotInvoice().getSlotId()).orElseThrow();
                    slot.setStatus(StatusSlotEnum.AVAILABLE);
                    slot.setUser(null);
                    slot = slotRepository.save(slot);
                }
            }
        }
        return modelMapper.map(payment, PaymentResponse.class);
    }

    public Payment create(Invoice invoice) {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setCreateTime(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPrice(invoice.getPrice());
        return paymentRepository.save(payment);
    }

    public String getPaymentUrl(Invoice invoice) {
        if (invoice.getStatus() == PaymentStatus.SUCCESS) {
            throw new AppException("INVOICE_ALREADY_PAID");
        }
        if (invoice.getStatus() == PaymentStatus.CANCEL) {
            throw new AppException("INVOICE_CANCEL");
        }
        var payment = paymentRepository.findLatestByInvoice(invoice).orElse(null);

        // booking invoice expire
        if (invoice.getStatus() == PaymentStatus.PENDING && invoice.getType() == InvoiceType.BOOKING && invoice.getExpireTime() != null && LocalDateTime.now().isAfter(invoice.getExpireTime())) {
            invoice.setStatus(PaymentStatus.CANCEL);
            invoice = invoiceRepository.save(invoice);
            if (payment != null) {
                payment.setStatus(PaymentStatus.CANCEL);
                payment = paymentRepository.save(payment);
            }
            if (invoice.getType() == InvoiceType.BOOKING) {
                SlotInvoice slotInvoice = slotInvoiceRepository.findById(invoice.getSlotInvoice().getId()).orElseThrow();
                Slot slot = slotRepository.findById(slotInvoice.getSlotId()).orElseThrow();
                slotService.unlock(slot);
            }
            throw new AppException("INVOICE_EXPIRE");
        }

        if (payment == null || payment.getCreateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES) >= 10) {
            payment = create(invoice);
        }
        return vnPayService.createPaymentUrl(payment.getId(), payment.getCreateTime(), payment.getPrice());
    }

    public String getPaymentUrl(UUID id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow(() -> new AppException("INVOICE_NOT_FOUND"));
        return getPaymentUrl(invoice);
    }

    public String getPaymentUrl(Payment payment) {
        return vnPayService.createPaymentUrl(payment.getId(), payment.getCreateTime(), payment.getPrice());
    }
}

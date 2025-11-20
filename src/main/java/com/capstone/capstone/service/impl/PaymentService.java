package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.SlotInvoiceRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        if (invoice.getStatus() == PaymentStatus.PENDING && invoice.getType() == InvoiceType.BOOKING && invoice.getCreateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES) >= 10) {
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
}

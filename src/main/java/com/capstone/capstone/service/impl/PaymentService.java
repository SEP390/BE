package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class PaymentService {
    private final InvoiceService invoiceService;
    private final SlotInvoiceService slotInvoiceService;
    private final InvoiceRepository invoiceRepository;
    private final VNPayService vnPayService;
    private final ModelMapper modelMapper;
    private final SlotService slotService;
    private final SlotRepository slotRepository;

    public InvoiceResponse handle(VNPayResult res) {
        var invoiceId = res.getId();
        var vnPayStatus = res.getStatus();
        var invoice = invoiceRepository.findById(invoiceId).orElseThrow();
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            invoice = invoiceService.updateStatus(invoice, vnPayStatus);
            if (invoice.getType() == InvoiceType.BOOKING) {
                slotInvoiceService.onPayment(invoice, invoice.getStatus());
            }
        }
        InvoiceResponse response = modelMapper.map(invoice, InvoiceResponse.class);
        if (invoice.getType() == InvoiceType.BOOKING) {
            response.setSlotInvoice(slotInvoiceService.toResponse(invoice.getSlotInvoice()));
        }
        return response;
    }

    @Transactional
    public InvoiceResponse handle(HttpServletRequest request) {
        var res = vnPayService.verify(request);
        return handle(res);
    }

    @Transactional
    public String getPendingBookingUrl() {
        User user = SecurityUtils.getCurrentUser();
        Invoice invoice = invoiceRepository.findByUserAndTypeAndStatus(user, InvoiceType.BOOKING, PaymentStatus.PENDING).orElseThrow(() -> new AppException("NO_INVOICE"));
        // hết hạn
        if (invoice.getCreateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES) >= 10) {
            invoice.setStatus(PaymentStatus.CANCEL);
            invoice = invoiceRepository.save(invoice);
            Slot slot = slotRepository.findById(invoice.getSlotInvoice().getSlotId()).orElseThrow();
            slotService.unlock(slot);
            return null;
        }
        return vnPayService.createPaymentUrl(invoice.getId(), invoice.getCreateTime(), invoice.getPrice());
    }
}

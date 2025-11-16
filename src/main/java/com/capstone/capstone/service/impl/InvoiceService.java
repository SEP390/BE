package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public Invoice create(User user, long price, String reason, InvoiceType type) {
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setPrice(price);
        invoice.setType(type);
        invoice.setReason(reason);
        invoice.setStatus(PaymentStatus.PENDING);
        invoice.setCreateTime(LocalDateTime.now());
        return invoiceRepository.save(invoice);
    }

    public Invoice updateStatus(Invoice invoice, VNPayStatus status) {
        if (status == VNPayStatus.SUCCESS) invoice.setStatus(PaymentStatus.SUCCESS);
        else invoice.setStatus(PaymentStatus.CANCEL);
        return invoiceRepository.save(invoice);
    }
}

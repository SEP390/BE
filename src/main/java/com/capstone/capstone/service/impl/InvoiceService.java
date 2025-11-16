package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ModelMapper modelMapper;

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

    public PagedModel<InvoiceResponse> getAll(Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        return new PagedModel<>(invoiceRepository.findAll((r, q, c) -> {
            return c.equal(r.get("user"), user);
        }, pageable).map(invoice -> modelMapper.map(invoice, InvoiceResponse.class)));
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

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

    public InvoiceResponse create(CreateInvoiceRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow(() -> new AppException("USER_NOT_FOUND"));
        Invoice invoice = create(user, request.getPrice(), request.getReason(), request.getType());
        return modelMapper.map(invoice, InvoiceResponse.class);
    }

    public Invoice getPendingBookingInvoice() {
        User user = SecurityUtils.getCurrentUser();
        return invoiceRepository.findByUserAndTypeAndStatus(user, InvoiceType.BOOKING, PaymentStatus.PENDING).orElseThrow(() -> new AppException("NO_INVOICE"));
    }

    public PagedModel<InvoiceResponse> getAllByUser(Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        return new PagedModel<>(invoiceRepository.findAll((r, q, c) -> {
            return c.equal(r.get("user"), user);
        }, pageable).map(invoice -> modelMapper.map(invoice, InvoiceResponse.class)));
    }

    public PagedModel<InvoiceResponseJoinUser> getAll(Pageable pageable) {
        return new PagedModel<>(invoiceRepository.findAll(pageable).map(invoice -> modelMapper.map(invoice, InvoiceResponseJoinUser.class)));
    }

    public InvoiceCountResponse count() {
        InvoiceCountResponse res = new InvoiceCountResponse();
        res.setTotalCount(invoiceRepository.count());
        res.setTotalSuccess(invoiceRepository.count((r, q, c) -> c.equal(r.get("status"), PaymentStatus.SUCCESS)));
        res.setTotalPending(invoiceRepository.count((r, q, c) -> c.equal(r.get("status"), PaymentStatus.PENDING)));
        return res;
    }

    public boolean authorize(UUID id) {
        var user = SecurityUtils.getCurrentUser();
        return invoiceRepository.exists((r, q, c) -> {
            return c.and(c.equal(r.get("id"), id), c.equal(r.get("user"), user));
        });
    }

}

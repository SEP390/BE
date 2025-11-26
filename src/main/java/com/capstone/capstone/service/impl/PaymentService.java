package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.PaymentResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
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
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final InvoiceChangeService invoiceChangeService;

    @Transactional
    public PaymentResponse handle(VNPayResult res) {
        var paymentId = res.getId();
        var payment = paymentRepository.findById(paymentId).orElseThrow();
        var invoice = payment.getInvoice();
        var vnPayStatus = res.getStatus();
        // chỉ cập nhật nếu đang PENDING (double render problem)
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            PaymentStatus newStatus = vnPayStatus == VNPayStatus.SUCCESS ? PaymentStatus.SUCCESS : PaymentStatus.CANCEL;
            payment.setStatus(newStatus);
            payment = paymentRepository.save(payment);
            invoiceChangeService.update(invoice, newStatus);
        }
        return modelMapper.map(payment, PaymentResponse.class);
    }

    @Transactional
    public PaymentResponse handle(HttpServletRequest request) {
        var res = vnPayService.verify(request);
        return handle(res);
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

        // invoice expire
        if (invoice.getStatus() == PaymentStatus.PENDING && invoice.getExpireTime() != null && LocalDateTime.now().isAfter(invoice.getExpireTime())) {
            invoice.setStatus(PaymentStatus.CANCEL);
            invoice = invoiceRepository.save(invoice);
            if (payment != null) {
                payment.setStatus(PaymentStatus.CANCEL);
                payment = paymentRepository.save(payment);
            }
            invoice = invoiceChangeService.update(invoice, PaymentStatus.CANCEL);
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

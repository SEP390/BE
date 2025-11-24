package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class InvoiceChangeService {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public String update(UUID id, @Valid UpdateInvoiceRequest request) {
        VNPayResult res = new VNPayResult();
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            Payment payment = paymentRepository.findLatestByInvoice(invoice).orElseThrow();
            res.setId(payment.getId());
            res.setStatus(request.getStatus());
            paymentService.handle(res);
        }
        return "SUCCESS";
    }
}

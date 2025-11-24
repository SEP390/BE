package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class InvoiceExpireService implements Runnable {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final Invoice invoice;

    @Override
    public void run() {
        log.info("Cancel invoice {}", invoice.getId());
        Invoice current = invoiceRepository.findById(invoice.getId()).orElseThrow();
        if (current.getStatus() == PaymentStatus.PENDING) {
            current.setStatus(PaymentStatus.CANCEL);
            invoiceRepository.save(current);
            List<Payment> payments = paymentRepository.findAllByInvoice(current);
            for (Payment payment : payments) {
                payment.setStatus(PaymentStatus.CANCEL);
            }
            paymentRepository.saveAll(payments);
        }
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.SlotInvoice;
import com.capstone.capstone.repository.InvoiceRepository;
import com.capstone.capstone.repository.PaymentRepository;
import com.capstone.capstone.repository.SlotRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service chịu trách nhiệm cho việc thay đổi trạng thái invoice
 */
@Service
@AllArgsConstructor
public class InvoiceChangeService {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final SlotRepository slotRepository;
    private final SlotService slotService;
    private final InvoiceRepository invoiceRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public InvoiceResponse update(UUID id, @Valid UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        return modelMapper.map(update(invoice, request.getStatus()), InvoiceResponse.class);
    }

    public Invoice update(Invoice invoice, PaymentStatus status) {
        if (invoice.getStatus() == PaymentStatus.PENDING) {
            Payment payment = paymentRepository.findLatestByInvoice(invoice).orElse(null);
            if (status == PaymentStatus.SUCCESS) {
                invoice.setStatus(PaymentStatus.SUCCESS);
            } else {
                invoice.setStatus(PaymentStatus.CANCEL);
            }
            invoice = invoiceRepository.save(invoice);
            if (payment != null) {
                if (status == PaymentStatus.SUCCESS) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                } else {
                    payment.setStatus(PaymentStatus.CANCEL);
                }
                payment = paymentRepository.save(payment);
            }
            if (invoice.getType() == InvoiceType.BOOKING) {
                SlotInvoice slotInvoice = invoice.getSlotInvoice();
                Slot slot = slotRepository.findById(slotInvoice.getSlotId()).orElseThrow();
                if (invoice.getStatus() == PaymentStatus.SUCCESS) slotService.book(slot, slotInvoice.getSemester());
                else if (invoice.getStatus() == PaymentStatus.CANCEL) slotService.unlock(slot);
            }
        }
        return invoice;
    }
}

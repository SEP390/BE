package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.response.booking.InvoiceResponse;
import com.capstone.capstone.dto.response.booking.PaymentVerifyResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.Invoice;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.mapper.InvoiceMapper;
import com.capstone.capstone.repository.InvoiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PaymentService {
    private final InvoiceRepository invoiceRepository;
    private final VNPayService vNPayService;
    private final SlotHistoryService slotHistoryService;
    private final SlotService slotService;
    private final RoomPricingService roomPricingService;
    private final InvoiceMapper invoiceMapper;

    /**
     * Create invoice for slot
     */
    public Invoice createForSlot(User user, Slot slot) {
        long price = roomPricingService.getPriceOfRoom(slot.getRoom());
        Invoice invoice = new Invoice();
        LocalDateTime now = LocalDateTime.now();
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setUser(user);
        invoice.setPrice(price);
        invoice.setCreateDate(now);
        invoice = invoiceRepository.save(invoice);
        slot.setInvoice(invoice);
        slotService.save(slot);
        return invoice;
    }

    /**
     * Create payment url for invoice
     */
    public String createPaymentUrl(Invoice invoice) {
        return vNPayService.createPaymentUrl(invoice.getId(), invoice.getCreateDate(), invoice.getPrice());
    }

    public Invoice getById(UUID id) {
        return invoiceRepository.findById(id).orElseThrow();
    }

    @Transactional
    public PaymentVerifyResponse verifyForSlot(HttpServletRequest request, User user) {
        var result = vNPayService.verify(request);
        var invoice = invoiceRepository.findById(result.getId()).orElseThrow();
        var slot = slotService.findByInvoice(invoice);
        if (!invoice.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        if (invoice.getStatus() == InvoiceStatus.PENDING && result.getStatus() == VNPayStatus.SUCCESS) {
            successForSlot(slot, invoice);
        }

        if (invoice.getStatus() == InvoiceStatus.PENDING && result.getStatus() != VNPayStatus.SUCCESS) {
            failForSlot(slot, invoice);
        }
        return PaymentVerifyResponse.builder()
                .dormName(slot.getRoom().getDorm().getDormName())
                .slotName(slot.getSlotName())
                .floor(slot.getRoom().getFloor())
                .roomNumber(slot.getRoom().getRoomNumber())
                .status(result.getStatus())
                .price(invoice.getPrice())
                .build();
    }

    @Transactional
    public void successForSlot(Slot slot, Invoice invoice) {
        slot.setStatus(StatusSlotEnum.UNAVAILABLE);
        slotService.save(slot);
        invoice.setStatus(InvoiceStatus.SUCCESS);
        invoiceRepository.save(invoice);
        slotHistoryService.create(slot, invoice);
    }

    public void failForSlot(Slot slot, Invoice invoice) {
        invoice.setStatus(InvoiceStatus.CANCEL);
        invoiceRepository.save(invoice);
        slotService.unlock(slot);
    }

    public Page<InvoiceResponse> history(User user, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return invoiceRepository.findByUser(user, pageable).map(invoiceMapper::toInvoiceResponse);
    }
}

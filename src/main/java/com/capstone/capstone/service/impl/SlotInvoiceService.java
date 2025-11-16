package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.SlotInvoiceResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.SlotInvoiceRepository;
import com.capstone.capstone.repository.SlotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SlotInvoiceService {
    private final SlotInvoiceRepository slotInvoiceRepository;
    private final InvoiceService invoiceService;
    private final SlotService slotService;
    private final SlotRepository slotRepository;
    private final VNPayService vnPayService;

    public String createPaymentUrl(User user, Slot slot, Semester semester) {
        var price = slot.getRoom().getPricing().getPrice();
        Invoice invoice = invoiceService.create(user, price, "Đặt phòng %s".formatted(slot.getRoom().getRoomNumber()), InvoiceType.BOOKING);
        SlotInvoice slotInvoice = new SlotInvoice();
        slotInvoice.setUser(user);
        slotInvoice.setSlotId(slot.getId());
        slotInvoice.setSlotName(slot.getSlotName());
        slotInvoice.setRoom(slot.getRoom());
        slotInvoice.setPrice(price);
        slotInvoice.setSemester(semester);
        slotInvoice.setInvoice(invoice);
        slotInvoiceRepository.save(slotInvoice);
        return vnPayService.createPaymentUrl(invoice.getId(), invoice.getCreateTime(), invoice.getPrice());
    }

    public void onPayment(Invoice invoice, PaymentStatus status) {
        SlotInvoice slotInvoice = invoice.getSlotInvoice();
        Slot slot = slotRepository.findById(slotInvoice.getSlotId()).orElseThrow();
        if (status == PaymentStatus.SUCCESS) slotService.book(slot, slotInvoice.getSemester());
        else if (status == PaymentStatus.CANCEL) slotService.unlock(slot);
    }

    public SlotInvoiceResponse toResponse(SlotInvoice slotInvoice) {
        SlotInvoiceResponse response = new SlotInvoiceResponse();
        response.setPrice(slotInvoice.getPrice());
        response.setSemesterName(slotInvoice.getSemester().getName());
        response.setRoomNumber(slotInvoice.getRoom().getRoomNumber());
        response.setDormName(slotInvoice.getRoom().getDorm().getDormName());
        response.setSlotName(slotInvoice.getSlotName());
        return response;
    }
}

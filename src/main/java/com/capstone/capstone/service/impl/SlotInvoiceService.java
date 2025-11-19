package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.SlotInvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SlotInvoiceService {
    private final SlotInvoiceRepository slotInvoiceRepository;
    private final InvoiceService invoiceService;

    public SlotInvoice create(User user, Slot slot, Semester semester) {
        var price = slot.getRoom().getPricing().getPrice();
        // tạo hóa đơn
        Invoice invoice = invoiceService.create(user, price, "Đặt phòng %s".formatted(slot.getRoom().getRoomNumber()), InvoiceType.BOOKING);

        SlotInvoice slotInvoice = new SlotInvoice();
        slotInvoice.setUser(user);
        slotInvoice.setSlotId(slot.getId());
        slotInvoice.setSlotName(slot.getSlotName());
        slotInvoice.setRoom(slot.getRoom());
        slotInvoice.setPrice(price);
        slotInvoice.setSemester(semester);
        slotInvoice.setInvoice(invoice);
        return slotInvoiceRepository.save(slotInvoice);
    }

}

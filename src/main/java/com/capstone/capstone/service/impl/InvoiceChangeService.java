package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.invoice.UpdateInvoiceRequest;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvoiceChangeService {
    private final InvoiceRepository invoiceRepository;
    private final SlotRepository slotRepository;
    private final SlotHistoryRepository slotHistoryRepository;
    private final EWUsageRepository ewUsageRepository;
    private final SemesterRepository semesterRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public Invoice onChange(Invoice invoice) {
        if (invoice.getStatus() == PaymentStatus.SUCCESS) {
            // đặt phòng thành công
            if (invoice.getType() == InvoiceType.BOOKING) {
                UUID slotId = invoice.getSlotInvoice().getSlotId();
                Slot slot = slotRepository.findById(slotId).orElseThrow(() -> new AppException("SLOT_NOT_FOUND"));
                // chuyển slot sang trạng thái checkin
                slot.setStatus(StatusSlotEnum.CHECKIN);
                // tạo lịch sử
                SlotHistory slotHistory = new SlotHistory();
                slotRepository.save(slot);
                slotHistory.setSlotId(slot.getId());
                slotHistory.setSlotName(slot.getSlotName());
                slotHistory.setRoom(slot.getRoom());
                slotHistory.setUser(slot.getUser());
                slotHistory.setSemester(invoice.getSlotInvoice().getSemester());
                slotHistoryRepository.save(slotHistory);
            } else if (invoice.getType() == InvoiceType.EW) {
                // thanh toán điện nước thành công
                Semester semester = semesterRepository.findCurrent();
                List<EWUsage> usages = ewUsageRepository.findAllUnpaid(invoice.getUser(), semester.getStartDate(), semester.getEndDate());
                // cập nhật tất cả ewusage trong kỳ
                for (EWUsage usage : usages) {
                    usage.setPaid(true);
                }
                ewUsageRepository.saveAll(usages);
            }
        } else {
            // hủy thanh toán đặt phòng
            if (invoice.getType() == InvoiceType.BOOKING) {
                invoice.setStatus(PaymentStatus.CANCEL);
                invoice = invoiceRepository.save(invoice);
                Slot slot = slotRepository.findById(invoice.getSlotInvoice().getSlotId()).orElseThrow();
                slot.setStatus(StatusSlotEnum.AVAILABLE);
                slot.setUser(null);
                slot = slotRepository.save(slot);
            }
        }
        return invoice;
    }

    @Transactional
    public Invoice update(Invoice invoice, PaymentStatus status) {
        if (status == PaymentStatus.PENDING) throw new AppException("ALREADY_PENDING");
        invoice.setStatus(status);
        invoice = invoiceRepository.save(invoice);
        invoice = onChange(invoice);
        return invoice;
    }

    @Transactional
    public InvoiceResponseJoinUser update(UUID id, @Valid UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        update(invoice, request.getStatus());
        return modelMapper.map(invoice, InvoiceResponseJoinUser.class);
    }
}

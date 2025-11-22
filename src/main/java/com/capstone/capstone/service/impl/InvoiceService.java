package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceRequest;
import com.capstone.capstone.dto.request.invoice.CreateInvoiceSubject;
import com.capstone.capstone.dto.response.invoice.InvoiceCountResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.invoice.InvoiceResponseJoinUser;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.exception.AppException;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final SemesterService semesterService;
    private final EWUsageRepository ewUsageRepository;
    private final EWPriceRepository eWPriceRepository;
    private final RoomRepository roomRepository;

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

    public void createElectricInvoice(User user, Semester semester, EWPrice price) {
        boolean containPaid = ewUsageRepository.containsPaid(user, semester.getStartDate(), semester.getEndDate());
        List<EWUsage> usages = ewUsageRepository.findAllUnpaid(user, semester.getStartDate(), semester.getEndDate());
        usages.sort(Comparator.comparing(EWUsage::getStartDate));
        int totalElectricUsed = usages.stream().mapToInt(EWUsage::getElectric).sum();
        int totalWaterUsed = usages.stream().mapToInt(EWUsage::getWater).sum();
        long priceToPay = 0;
        // đã từng trả tiền trước đó -> hiện tại cũng đã vượt quá mức
        if (containPaid) {
            priceToPay = totalElectricUsed * price.getElectricPrice() + totalWaterUsed * price.getWaterPrice();
        } else {
            if (totalElectricUsed > price.getMaxElectricIndex()) {
                priceToPay += (totalElectricUsed - price.getMaxElectricIndex()) * price.getMaxElectricIndex();
            }
            if (totalWaterUsed > price.getMaxWaterIndex()) {
                priceToPay += (totalWaterUsed - price.getMaxWaterIndex()) * price.getMaxWaterIndex();
            }
        }
        if (priceToPay > 0) {
            String startDate = usages.getFirst().getStartDate().format(DateTimeFormatter.ISO_DATE);
            String endDate = usages.getLast().getEndDate().format(DateTimeFormatter.ISO_DATE);
            create(user, priceToPay, "Tiền điện nước từ ngày %s đến ngày %s".formatted(startDate, endDate), InvoiceType.EW);
        }
    }

    public String create(CreateInvoiceRequest request) {
        Semester semester = semesterService.getCurrent().orElseThrow(() -> new AppException("CURRENT_SEMESTER_NOT_FOUND"));
        List<User> users;
        if (request.getSubject() == CreateInvoiceSubject.ALL) {
            users = userRepository.findAll();
        } else if (request.getSubject() == CreateInvoiceSubject.ROOM) {
            Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new AppException("ROOM_NOT_FOUND"));
            users = roomRepository.findUsers(room);
        } else if (request.getSubject() == CreateInvoiceSubject.USER) {
            users = request.getUsers().stream().map(id -> userRepository.findById(id).orElseThrow(() -> new AppException("USER_NOT_FOUND", id))).toList();
        } else throw new AppException("NO_SUBJECT");
        if (request.getType() == InvoiceType.EW) {
            EWPrice price = eWPriceRepository.getCurrent().orElseThrow(() -> new AppException("EW_PRICE_NOT_FOUND"));
            for (User user : users) {
                createElectricInvoice(user, semester, price);
            }
        } else {
            for (User user : users) {
                create(user, request.getPrice(), request.getReason(), request.getType());
            }
        }
        return "SUCCESS";
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

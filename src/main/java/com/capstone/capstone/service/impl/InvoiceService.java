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
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final SlotInvoiceRepository slotInvoiceRepository;

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
        var query = new SpecQuery<Invoice>();
        query.equal("user", user);
        query.equal("type", InvoiceType.EW);
        query.equal("status", PaymentStatus.PENDING);
        // đang có hóa đơn chưa trả, ko tạo
        if (invoiceRepository.exists(query.and())) return;
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
                priceToPay += (totalElectricUsed - price.getMaxElectricIndex()) * price.getElectricPrice();
            }
            if (totalWaterUsed > price.getMaxWaterIndex()) {
                priceToPay += (totalWaterUsed - price.getMaxWaterIndex()) * price.getWaterPrice();
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
            users = request.getUsers().stream().map(id -> userRepository.findById(id.getUserId()).orElseThrow(() -> new AppException("USER_NOT_FOUND", id))).toList();
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

    public PagedModel<InvoiceResponse> getAllByUser(Map<String, Object> filter, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        var query = new SpecQuery<Invoice>();
        query.equal("user", user);
        query.equal(filter, "status");
        query.equal(filter, "type");
        query.timeBetweenDate(filter, "createTime", "startDate", "endDate");
        return new PagedModel<>(invoiceRepository.findAll(query.and(), pageable).map(invoice -> modelMapper.map(invoice, InvoiceResponse.class)));
    }

    public PagedModel<InvoiceResponseJoinUser> getAll(Map<String, Object> filter, Pageable pageable) {
        var query = new SpecQuery<Invoice>();
        query.equal(filter, r -> r.get("user").get("id"), "userId");
        query.equal(filter, "status");
        query.equal(filter, "type");
        query.timeBetweenDate(filter, "createTime", "startDate", "endDate");
        return new PagedModel<>(invoiceRepository.findAll(query.and(), pageable).map(invoice -> modelMapper.map(invoice, InvoiceResponseJoinUser.class)));
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

    @Transactional
    public Invoice create(User user, Slot slot, Semester nextSemester) {
        long price = Optional.ofNullable(slot.getRoom()).map(Room::getPricing).map(RoomPricing::getPrice).orElseThrow(() -> new AppException("PRICE_NOT_FOUND"));
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setPrice(price);
        invoice.setType(InvoiceType.BOOKING);
        invoice.setReason("Đặt phòng %s kỳ %s".formatted(slot.getRoom().getRoomNumber(), nextSemester.getName()));
        invoice.setStatus(PaymentStatus.PENDING);
        var now = LocalDateTime.now();
        invoice.setCreateTime(now);
        // thời gian hết hạn
        invoice.setExpireTime(now.plusMinutes(10));
        invoice = invoiceRepository.save(invoice);
        // thông tin bổ sung
        SlotInvoice slotInvoice = new SlotInvoice();
        slotInvoice.setUser(user);
        slotInvoice.setSlotId(slot.getId());
        slotInvoice.setSlotName(slot.getSlotName());
        slotInvoice.setRoom(slot.getRoom());
        slotInvoice.setPrice(price);
        slotInvoice.setSemester(nextSemester);
        slotInvoice.setInvoice(invoice);
        slotInvoice = slotInvoiceRepository.save(slotInvoice);
        return invoice;
    }

    public InvoiceCountResponse userCount() {
        User user = SecurityUtils.getCurrentUser();
        InvoiceCountResponse res = new InvoiceCountResponse();
        res.setTotalCount(invoiceRepository.count((r, q, c) -> {
            return c.equal(r.get("user"), user);
        }));
        res.setTotalSuccess(invoiceRepository.count((r, q, c) -> c.and(c.equal(r.get("user"), user), c.equal(r.get("status"), PaymentStatus.SUCCESS))));
        res.setTotalPending(invoiceRepository.count((r, q, c) -> c.and(c.equal(r.get("user"), user), c.equal(r.get("status"), PaymentStatus.PENDING))));
        return res;
    }
}

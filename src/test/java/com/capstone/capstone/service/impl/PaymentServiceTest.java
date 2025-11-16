package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.InvoiceType;
import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.response.invoice.InvoiceResponse;
import com.capstone.capstone.dto.response.vnpay.VNPayResult;
import com.capstone.capstone.dto.response.vnpay.VNPayStatus;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentServiceTest {
    User user;
    Invoice invoice;
    SlotInvoice slotInvoice;
    Semester semester;
    Slot slot;
    Room room;
    Dorm dorm;

    @Autowired
    SlotRepository slotRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SemesterRepository semesterRepository;
    @Autowired
    SlotInvoiceRepository slotInvoiceRepository;

    @Autowired
    PaymentService paymentService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private DormRepository dormRepository;

    @Test
    void handle() {
        user = userRepository.save(new User());
        slot = new Slot();
        slot.setSlotName("Slot Test");
        slot = slotRepository.save(slot);
        dorm = new Dorm();
        dorm.setDormName("Dorm Test");
        dorm = dormRepository.save(dorm);
        room = new Room();
        room.setRoomNumber("Room Test");
        room.setDorm(dorm);
        room = roomRepository.save(room);
        semester = new Semester();
        semester.setName("Semester Test");
        semester = semesterRepository.save(semester);
        invoice = new Invoice();
        invoice.setUser(user);
        invoice.setType(InvoiceType.BOOKING);
        invoice.setStatus(PaymentStatus.PENDING);
        invoice.setPrice(100000L);
        invoice.setReason("Booking");
        invoice.setCreateTime(LocalDateTime.now());
        invoice = invoiceRepository.save(invoice);
        slotInvoice = new SlotInvoice();
        slotInvoice.setInvoice(invoice);
        slotInvoice.setSlotId(slot.getId());
        slotInvoice.setRoom(room);
        slotInvoice.setSemester(semester);
        slotInvoice.setPrice(100000L);
        slotInvoice = slotInvoiceRepository.save(slotInvoice);
        invoice.setSlotInvoice(slotInvoice);
        invoice = invoiceRepository.save(invoice);

        VNPayResult result = new VNPayResult();
        result.setId(invoice.getId());
        result.setStatus(VNPayStatus.SUCCESS);
        InvoiceResponse response = paymentService.handle(result);

        log.info("{}", response);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(InvoiceType.BOOKING);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
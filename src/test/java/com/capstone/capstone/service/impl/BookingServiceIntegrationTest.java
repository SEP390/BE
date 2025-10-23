package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.PaymentStatus;
import com.capstone.capstone.dto.enums.StatusSlotEnum;
import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.entity.*;
import com.capstone.capstone.repository.*;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class BookingServiceIntegrationTest {
    @Autowired
    BookingService bookingService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DormRepository dormRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    SlotRepository slotRepository;

    User user;
    Dorm dorm;
    Room room;
    Slot slot;

    MockedStatic<AuthenUtil> authenUtilMockedStatic = Mockito.mockStatic(AuthenUtil.class);

    @BeforeEach
    void setup() {
        user = new User();
        user.setUsername("tester");
        user = userRepository.save(user);
        dorm = new Dorm();
        dorm.setDormName("Dorm Test");
        dorm.setTotalRoom(1);
        dorm.setTotalFloor(1);
        dorm = dormRepository.save(dorm);
        room = new Room();
        room.setDorm(dorm);
        room.setRoomNumber("Room Test");
        room.setFloor(1);
        room.setTotalSlot(2);
        room = roomRepository.save(room);
        slot = new Slot();
        slot.setRoom(room);
        slot.setSlotName("Slot Test");
        slot.setStatus(StatusSlotEnum.AVAILABLE);
        slot = slotRepository.save(slot);
    }

    @Test
    void create(@Autowired SlotHistoryRepository slotHistoryRepository, @Autowired PaymentRepository paymentRepository) {
        authenUtilMockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(user.getId());
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSlotId(slot.getId());
        CreateBookingResponse response = bookingService.create(request);

        SlotHistory slotHistory = slotHistoryRepository.findOne((r, q, c) -> c.equal(r.get("slot"), slot)).orElseThrow();
        Assertions.assertNotNull(slotHistory);
        Assertions.assertEquals(slotHistory.getSlot().getId(), slot.getId());
        Assertions.assertNotNull(slotHistory.getPrice());
        Payment payment = paymentRepository.findOne((r, q, c) -> c.equal(r.get("slotHistory"), slotHistory)).orElseThrow();
        Assertions.assertNotNull(payment);
        Assertions.assertEquals(PaymentStatus.PENDING, payment.getStatus());
        Assertions.assertNotNull(response.getPaymentUrl());
    }

    @Test
    void current() {
        user = userRepository.findByUsername("resident").orElseThrow();
        authenUtilMockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(user.getId());
        System.out.println(bookingService.current());
    }
}
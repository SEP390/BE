package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.booking.CreateBookingRequest;
import com.capstone.capstone.dto.response.booking.CreateBookingResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.AuthenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("dev")
class BookingServiceIntegrationTest {
    @Autowired
    BookingService bookingService;
    @Autowired
    UserRepository userRepository;

    User user;
    UUID slotId = UUID.fromString("0047f8fe-670e-4245-8190-261b74ac59f8");

    MockedStatic<AuthenUtil> authenUtilMockedStatic = Mockito.mockStatic(AuthenUtil.class);

    @Test
    void create() {
        user = userRepository.findByUsername("resident").orElseThrow();
        authenUtilMockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(user.getId());
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSlotId(slotId);
        CreateBookingResponse response = bookingService.create(request);
        Assertions.assertNotNull(response.getPaymentUrl());
    }

    @Test
    void current() {
        user = userRepository.findByUsername("resident").orElseThrow();
        authenUtilMockedStatic.when(AuthenUtil::getCurrentUserId).thenReturn(user.getId());
        System.out.println(bookingService.current());
    }
}
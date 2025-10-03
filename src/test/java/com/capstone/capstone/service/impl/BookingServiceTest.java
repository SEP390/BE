package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@ActiveProfiles("dev")
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SlotRepository slotRepository;

    @Test
    void createBooking() {
        User user = userRepository.findById(UUID.fromString("0e7ba193-f3e9-4832-85ed-cc62d5487b1f")).get();
        Slot slot = slotRepository.findById(UUID.fromString("000797fa-4999-426f-8bdb-e03f7402dab9")).get();
        log.info("Slot: {}", slot.getId());
        var resp = bookingService.createBooking(user.getId(), slot.getId());
        log.info("Response: {}", resp);
    }
}
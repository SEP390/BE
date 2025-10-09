package com.capstone.capstone.service.impl;

import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class RoomServiceTest {
    @Autowired
    private RoomService roomService;

    private User user;

    @BeforeEach
    void setup(@Autowired UserRepository userRepository) {
        User example = new User();
        example.setUsername("resident");
        user = userRepository.findOne(Example.of(example)).orElseThrow();
    }

    /**
     * Integration test
     */
    @Test
    void getRoomMatching() {
        roomService.getRoomMatching(user).forEach(System.out::println);
    }
}
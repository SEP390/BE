package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.GenderEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class DormServiceTest {

    @Autowired
    private DormService dormService;

    @Test
    void getBookableDorm() {
        dormService.getBookableDorm(6, GenderEnum.MALE).forEach(dorm -> log.info(dorm.toString()));
    }
}
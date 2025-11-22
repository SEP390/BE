package com.capstone.capstone.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("dev")
@SpringBootTest
@Transactional
class EWUsageRepositoryTest {
    @Autowired
    EWUsageRepository ewUsageRepository;

    @Test
    void containsPaid() {
        assertThat(ewUsageRepository.containsPaid(LocalDate.now(), LocalDate.now())).isEqualTo(true);
    }
}
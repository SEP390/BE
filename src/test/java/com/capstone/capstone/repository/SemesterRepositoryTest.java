package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SemesterRepositoryTest {
    @Autowired
    SemesterRepository semesterRepository;

    @Test
    void findNextSemester_Success() {
        // start time after today
        semesterRepository.save(
                new Semester("FA25",
                        LocalDate.of(2025, 10, 15),
                        LocalDate.of(2025, 12, 15))
        );

        assertThat(semesterRepository.findNextSemester().getName()).isEqualTo("FA25");
    }

    @Test
    void findNextSemester_Null() {
        // start time before today
        semesterRepository.save(
                new Semester("SU25",
                        LocalDate.of(2025, 3, 15),
                        LocalDate.of(2025, 5, 15))
        );

        assertThat(semesterRepository.findNextSemester()).isNull();
    }
}
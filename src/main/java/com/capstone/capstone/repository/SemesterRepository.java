package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    @Query("""
        FROM Semester
        WHERE startDate > CURRENT_DATE
        ORDER BY startDate DESC
        LIMIT 1
    """)
    Semester findNextSemester();

    @Query("""
        SELECT s FROM Semester s
        WHERE :currentDate BETWEEN s.startDate AND s.endDate
""")
    Optional<Semester> findSemesterByCurrentDate(@Param("currentDate") LocalDate currentDate);
}

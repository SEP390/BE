package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID>, JpaSpecificationExecutor<Semester> {
    @Query("""
            FROM Semester
            WHERE startDate > CURRENT_DATE
            ORDER BY startDate ASC
            LIMIT 1
            """)
    Semester findNextSemester();

    @Query("""
            SELECT s FROM Semester s
            WHERE :currentDate BETWEEN s.startDate AND s.endDate
            """)
    Optional<Semester> findSemesterByCurrentDate(@Param("currentDate") LocalDate currentDate);

    @Query("""
            FROM Semester
            WHERE CURRENT_DATE >= startDate AND CURRENT_DATE <= endDate
            ORDER BY startDate DESC
            LIMIT 1
            """)
    Semester findCurrent();

    @Query("""
            FROM Semester
            WHERE CURRENT_DATE > endDate
            ORDER BY startDate DESC
            LIMIT 1
            """)
    Optional<Semester> findPrevious();
}

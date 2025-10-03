package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    @Query("""
        FROM Semester
        WHERE startDate > CURRENT_DATE
        ORDER BY startDate DESC
        LIMIT 1
    """)
    Semester findNextSemester();
}

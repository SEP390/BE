package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Payment;
import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Page<Payment> findByUser(User user, Pageable pageable);

    @Query("""
    FROM Payment p
    JOIN p.slotHistory s
    WHERE s.semester = :semester AND p.user = :user
    ORDER BY p.createDate DESC
    LIMIT 1
    """)
    Payment findCurrentBooking(User user, Semester semester);
}

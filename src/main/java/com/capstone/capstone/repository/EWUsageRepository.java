package com.capstone.capstone.repository;

import com.capstone.capstone.entity.EWUsage;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EWUsageRepository extends JpaRepository<EWUsage, UUID>, JpaSpecificationExecutor<EWUsage> {
    @Query("""
            FROM EWUsage eu
            WHERE eu.user = :user AND eu.startDate >= :startDate AND eu.endDate <= :endDate AND eu.paid = false
            """)
    List<EWUsage> findAllUnpaid(User user, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT CASE WHEN (COUNT(*) > 0) THEN TRUE ELSE FALSE END
            FROM EWUsage eu
            WHERE eu.user = :user AND eu.startDate >= :startDate AND eu.endDate <= :endDate AND eu.paid = true
            """)
    boolean containsPaid(User user, LocalDate startDate, LocalDate endDate);
}

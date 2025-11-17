package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SlotHistoryRepository extends JpaRepository<SlotHistory, UUID>, JpaSpecificationExecutor<SlotHistory> {
    @Query("""
            FROM SlotHistory sh
            WHERE sh.user = :user AND sh.slotId = :slotId
            ORDER BY sh.semester.startDate DESC
            LIMIT 1
            """)
    Optional<SlotHistory> getLatest(User user, UUID slotId);
}

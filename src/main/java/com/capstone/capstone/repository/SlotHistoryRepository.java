package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SlotHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SlotHistoryRepository extends JpaRepository<SlotHistory, UUID> {
    @Query("""
    FROM SlotHistory sh
    JOIN FETCH sh.slot
    WHERE sh.user.id = :currentUserId
    ORDER BY sh.createDate DESC
    LIMIT 1
""")
    SlotHistory findCurrentSemesterSlotHistory(UUID currentUserId);

    @Query("""
        FROM SlotHistory sh
        JOIN FETCH sh.slot
        JOIN FETCH sh.slot.room
        JOIN FETCH sh.slot.room.dorm
        WHERE sh.id = :id
    """)
    SlotHistory findByIdAndFetchDetails(UUID id);

    UUID id(UUID id);
}

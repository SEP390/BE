package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.SlotHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SlotHistoryRepository extends JpaRepository<SlotHistory, UUID> {
    @Query("""
    FROM SlotHistory sh
    WHERE sh.user.id = :currentUserId AND sh.semester.id = :semesterId AND sh.status = com.capstone.capstone.dto.enums.StatusSlotHistoryEnum.SUCCESS
    ORDER BY sh.createDate DESC
    LIMIT 1
""")
    SlotHistory findCurrentSlotHistory(UUID currentUserId, UUID semesterId);

    @Query("""
        FROM SlotHistory sh
        JOIN FETCH sh.slot
        JOIN FETCH sh.slot.room
        JOIN FETCH sh.slot.room.dorm
        JOIN FETCH sh.semester
        WHERE sh.id = :id
    """)
    SlotHistory findByIdAndFetchDetails(UUID id);

    UUID id(UUID id);
}

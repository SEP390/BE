package com.capstone.capstone.repository;

import com.capstone.capstone.entity.Semester;
import com.capstone.capstone.entity.SlotHistory;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SlotHistoryRepository extends JpaRepository<SlotHistory, UUID> {
    /**
     * Find current {@link SlotHistory} in semester of user
     * @param user user to find
     * @param semester semester to find
     * @return SlotHistory
     */
    @Query("""
    FROM SlotHistory sh
    WHERE sh.user = :user
        AND sh.semester = :semester
    ORDER BY sh.createDate DESC
    LIMIT 1
""")
    SlotHistory findCurrentSlotHistory(User user, Semester semester);

    @Query("""
        FROM SlotHistory sh
        JOIN FETCH sh.slot
        JOIN FETCH sh.slot.room
        JOIN FETCH sh.slot.room.dorm
        JOIN FETCH sh.semester
        WHERE sh = :slotHistory
    """)
    SlotHistory findDetails(SlotHistory slotHistory);

    UUID id(UUID id);

    @Query("""
        FROM SlotHistory sh
        JOIN FETCH sh.slot
        JOIN FETCH sh.slot.room
        JOIN FETCH sh.slot.room.dorm
        JOIN FETCH sh.semester
        WHERE sh.user.id = :currentUserId
    """)
    List<SlotHistory> findAllByUser(UUID currentUserId);
}

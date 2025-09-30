package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.entity.Dorm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DormRepository extends JpaRepository<Dorm, UUID> {
    /**
     * Query dorm contain room with specific type, and only have user with specific gender
     *
     * @param totalSlot room type
     * @param gender    gender of users in room
     * @return dorm
     */
    @Query("""
            SELECT room.dorm
            FROM Room room
            JOIN Slot slot ON slot.room.id = room.id AND slot.status = 'AVAILABLE'
            LEFT JOIN User user ON user.id = slot.user.id AND (user.gender IS NULL OR user.gender = :gender)
            WHERE room.totalSlot = :totalSlot
            GROUP BY room.dorm
            """)
    List<Dorm> getBookableDorm(int totalSlot, GenderEnum gender);
}

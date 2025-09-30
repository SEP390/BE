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
            WHERE room.status = 0 AND NOT EXISTS (
                SELECT 1 FROM Slot slot
                JOIN User user ON slot.user.id = user.id
                WHERE slot.room.id = room.id AND user.gender != :gender
            )
            GROUP BY room.dorm
            ORDER BY room.dorm.dormName
            """)
    List<Dorm> getBookableDorm(int totalSlot, GenderEnum gender);
}

package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.dorm.BookableDormResponse;
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
        SELECT new com.capstone.capstone.dto.response.dorm.BookableDormResponse(
            d.id,
            d.dormName,
            d.totalRoom,
            d.totalFloor,
            CASE WHEN EXISTS (
                SELECT 1
                FROM Room r
                WHERE r.dorm = d
                    AND r.status = com.capstone.capstone.dto.enums.StatusRoomEnum.AVAILABLE
                    AND NOT EXISTS (
                        SELECT 1
                        FROM Slot s
                        WHERE s MEMBER OF r.slots AND s.user IS NOT NULL AND s.user.gender <> :gender
                    )
            ) THEN true ELSE false END
        )
        FROM Dorm d
        ORDER BY d.dormName
    """)
    List<BookableDormResponse> getBookableDorm(int totalSlot, GenderEnum gender);
}

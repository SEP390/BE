package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID>, JpaSpecificationExecutor<Room> {

    @Query("""
    SELECT r
    FROM Room r
    JOIN r.slots s
    LEFT JOIN s.user u
    WHERE r.status = com.capstone.capstone.dto.enums.StatusRoomEnum.AVAILABLE
    GROUP BY r
    HAVING SUM(CASE WHEN u IS NOT NULL AND u.gender <> :gender THEN 1 ELSE 0 END) = 0
    """)
    List<Room> findAvailableForGender(GenderEnum gender);

    // need to move to user repo
    @Query("""
    SELECT u
    FROM Room r
    JOIN r.slots s
    JOIN s.user u
    WHERE r = :room
    """)
    List<User> findUsers(Room room);

    @Query("""
    FROM Room r
    JOIN r.slots s
    WHERE s.user = :user
""")
    Room findByUser(User user);
}

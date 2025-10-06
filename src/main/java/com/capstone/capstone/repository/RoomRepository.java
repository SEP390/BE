package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.room.RoomMatching;
import com.capstone.capstone.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

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

    @Query(value = """
       SELECT
            r.id as id,
            r.dorm.dormName as dormName,
            r.floor as floor,
            r.roomNumber as roomNumber,
            CASE
               WHEN COUNT(slotUser.id) = 0 THEN 0.0
               ELSE (SUM(CAST((
                   SELECT COUNT(sqs2)
                   FROM SurveyQuetionSelected sqs1
                   JOIN SurveyQuetionSelected sqs2 ON sqs1.surveyOption.id = sqs2.surveyOption.id
                   WHERE sqs1.user.id = :currentUserId
                   AND sqs2.user.id = slotUser.id
               ) AS double)) / (20.0 * COUNT(slotUser.id))) * 100.0
           END as matching,
           (COUNT(*) - COUNT(s.user)) AS slotAvailable
        FROM Room r
        LEFT JOIN r.slots s
        LEFT JOIN s.user slotUser
        WHERE r.status = 'AVAILABLE'
            AND s.status = com.capstone.capstone.dto.enums.StatusSlotEnum.AVAILABLE
            AND (slotUser IS NULL OR slotUser.gender = (SELECT u.gender FROM User u WHERE u.id = :currentUserId))
            AND r.id NOT IN (
                SELECT sl.room.id FROM Slot sl
                JOIN sl.user su
                WHERE su.gender != (SELECT u2.gender FROM User u2 WHERE u2.id = :currentUserId)
            )
        GROUP BY r.id, r.roomNumber
        HAVING (COUNT(*) - COUNT(s.user)) > 0
        ORDER BY matching DESC
        LIMIT 5
    """)
    List<RoomMatching> findBookableRoomFirstYear(UUID currentUserId);

    @Query(value = """
        SELECT
            r.id as id,
            r.dorm.dormName as dormName,
            r.floor as floor,
            r.roomNumber as roomNumber,
            CASE
               WHEN COUNT(slotUser.id) = 0 THEN 0.0
               ELSE (SUM(CAST((
                   SELECT COUNT(sqs2)
                   FROM SurveyQuetionSelected sqs1
                   JOIN SurveyQuetionSelected sqs2 ON sqs1.surveyOption.id = sqs2.surveyOption.id
                   WHERE sqs1.user.id = :currentUserId
                   AND sqs2.user.id = slotUser.id
               ) AS double)) / (20.0 * COUNT(slotUser.id))) * 100.0
           END as matching,
           (COUNT(*) - COUNT(s.user)) AS slotAvailable
        FROM Room r
        LEFT JOIN r.slots s
        LEFT JOIN s.user slotUser
        WHERE r.status = 'AVAILABLE' AND r.dorm.id = :dormId AND r.totalSlot = :totalSlot AND r.floor = :floor
            AND s.status = com.capstone.capstone.dto.enums.StatusSlotEnum.AVAILABLE
            AND (slotUser IS NULL OR slotUser.gender = (SELECT u.gender FROM User u WHERE u.id = :currentUserId))
            AND r.id NOT IN (
                SELECT sl.room.id FROM Slot sl
                JOIN sl.user su
                WHERE su.gender != (SELECT u2.gender FROM User u2 WHERE u2.id = :currentUserId)
            )
        GROUP BY r.id, r.roomNumber
        HAVING (COUNT(*) - COUNT(s.user)) > 0
        ORDER BY matching DESC
    """)
    List<RoomMatching> findBookableRoom(UUID currentUserId, int totalSlot, UUID dormId, int floor);

    @Query("""
    FROM Room r
    JOIN FETCH r.dorm
    JOIN FETCH r.slots
    WHERE r.id = :id
""")
    Room findDetails(UUID id);

    @Query("""
    FROM Room r
    JOIN FETCH r.slots
    WHERE r = :room
    """)
    Room findSlots(Room room);
}

package com.capstone.capstone.repository;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.response.room.RoomDetails;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.User;
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

    // need to move to user repo
    @Query("""
    SELECT COUNT(sqs1) / :totalQuestion * 100
    FROM SurveyQuetionSelected sqs1
    JOIN SurveyQuetionSelected sqs2 ON sqs1.surveyOption = sqs2.surveyOption
    AND sqs1.user = :userA AND sqs2.user = :userB
    """)
    Double computeMatching(User userA, User userB, int totalQuestion);

    // need to move to user repo
    @Query("""
    SELECT u
    FROM Room r
    JOIN r.slots s
    JOIN s.user u
    WHERE r = :room
    """)
    List<User> findUsers(Room room);

    // need to move to survey question repo
    @Query("""
    SELECT COUNT(*) FROM SurveyQuestion
    """)
    int totalQuestion();

    @Query("""
    SELECT
        r.id as id,
        r.roomNumber as roomNumber,
        r.dorm.id as dormId,
        r.dorm.dormName as dormName,
        r.floor as floor,
        rp.price as price,
        r.totalSlot as totalSlot
    FROM Room r
    JOIN RoomPricing rp ON r.totalSlot = rp.totalSlot
    JOIN r.dorm
    JOIN r.slots s
    LEFT JOIN s.user u
    WHERE r IN :rooms
    """)
    List<RoomDetails> findDetails(List<Room> rooms);

    @Query("""
    FROM Room r
    JOIN FETCH r.dorm
    JOIN FETCH r.slots
    WHERE r.id = :id
    """)
    Room fetchDormAndSlots(UUID id);

    @Query("""
    FROM Room r
    JOIN FETCH r.slots
    WHERE r = :room
    """)
    Room fetchSlots(Room room);
}

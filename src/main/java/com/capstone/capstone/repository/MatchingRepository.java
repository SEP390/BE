package com.capstone.capstone.repository;

import com.capstone.capstone.dto.response.booking.UserMatching;
import com.capstone.capstone.dto.response.room.RoomMatching;
import com.capstone.capstone.entity.Room;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface MatchingRepository extends Repository<Room, UUID> {
    @Query("""
    SELECT
        u.id AS id,
        COUNT(sqs2) AS sameOptionCount
    FROM User u
    JOIN u.surveyQuestionSelected sqs1
    JOIN SurveyQuetionSelected sqs2 ON sqs2.user = :user AND sqs1.surveyOption = sqs2.surveyOption
    WHERE u IN :users
    GROUP BY u
    """)
    List<UserMatching> computeUserMatching(User user, List<User> users);

    @Query("""
    SELECT
        r.id AS roomId,
        COUNT(DISTINCT u) AS userCount,
        COUNT(sqs2) AS sameOptionCount
    FROM Room r
    JOIN r.slots s
    JOIN s.user u
    JOIN u.surveyQuestionSelected sqs
    JOIN SurveyQuetionSelected sqs2 ON sqs2.user = :user AND sqs.surveyOption = sqs2.surveyOption
    WHERE r in :rooms
    GROUP BY r
    """)
    List<RoomMatching> computeRoomMatching(User user, List<Room> rooms);
}

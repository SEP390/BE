package com.capstone.capstone.repository;

import com.capstone.capstone.dto.response.room.RoomMatching;
import com.capstone.capstone.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Query(value = """
        WITH current_user_answers AS (
            SELECT survey_option_id
            FROM survey_quetion_selected
            WHERE user_id = :currentUserId
        ),
        valid_room AS (
            SELECT DISTINCT r.*
            FROM room r
            LEFT JOIN slot s ON s.room_id = r.id
            LEFT JOIN user u ON u.id = s.user_id
            WHERE u.id IS NULL OR u.gender = (SELECT gender FROM user WHERE id = :currentUserId)
        ),
        room_user_pairs AS (
            SELECT r.id AS room_id, s.user_id AS other_user_id
            FROM valid_room r
            JOIN slot s ON s.room_id = r.id
            WHERE s.user_id IS NOT NULL
        ),
        shared_answers AS (
            SELECT rup.room_id, rup.other_user_id, COUNT(*) AS matching_answers
            FROM room_user_pairs rup
            JOIN survey_quetion_selected sqs
            ON sqs.user_id = rup.other_user_id
            JOIN current_user_answers cua
            ON cua.survey_option_id = sqs.survey_option_id
            GROUP BY rup.room_id, rup.other_user_id
        ),
        current_user_answer_count AS (
            SELECT COUNT(*) AS total_answers
            FROM current_user_answers
        ),
        matching_percentage_per_user AS (
            SELECT sa.room_id, sa.other_user_id,
                (sa.matching_answers / cua.total_answers) * 100 AS match_percentage
            FROM shared_answers sa
            CROSS JOIN current_user_answer_count cua
        ),
        final_room_matching AS (
            SELECT r.id AS id,
                r.room_number as roomNumber,
                AVG(mp.match_percentage) AS matching
            FROM valid_room r
            LEFT JOIN matching_percentage_per_user mp ON r.id = mp.room_id
            GROUP BY r.id, r.room_number
        )
        SELECT *
        FROM final_room_matching
        ORDER BY matching DESC
        LIMIT 5;
    """, nativeQuery = true)
    List<RoomMatching> findBookableRoomFirstYear(UUID currentUserId);

    @Query(value = """
            WITH current_user_answers AS (SELECT survey_option_id
                                          FROM survey_quetion_selected
                                          WHERE user_id = :currentUserId),
                valid_room  AS (
                    SELECT *
                    FROM room r
                    WHERE room.status = 0 AND NOT EXISTS(
                        SELECT 1 FROM slot s JOIN user u ON s.user_id = u.id
                                 WHERE s.room_id = r.id AND s.gender != :gender
                    )
                ),
                 room_user_pairs AS (SELECT r.id      AS room_id,
                                            s.user_id AS other_user_id
                                     FROM valid_room r
                                              JOIN slot s ON s.room_id = r.id
                                     WHERE s.user_id IS NOT NULL),
                 shared_answers AS (SELECT rup.room_id,
                                           rup.other_user_id,
                                           COUNT(*) AS matching_answers
                                    FROM room_user_pairs rup
                                             JOIN survey_quetion_selected sqs
                                                  ON sqs.user_id = rup.other_user_id
                                             JOIN current_user_answers cua
                                                  ON cua.survey_option_id = sqs.survey_option_id
                                    GROUP BY rup.room_id, rup.other_user_id),
                 current_user_answer_count AS (SELECT COUNT(*) AS total_answers
                                               FROM current_user_answers),
            
                 matching_percentage_per_user AS (SELECT sa.room_id,
                                                         sa.other_user_id,
                                                         (sa.matching_answers / cua.total_answers) * 100 AS match_percentage
                                                  FROM shared_answers sa
                                                           CROSS JOIN current_user_answer_count cua),
                 final_room_matching AS (SELECT r.id                     AS id,
                                                r.room_number            as roomNumber,
                                                AVG(mp.match_percentage) AS matching
                                         FROM room r
                                                  LEFT JOIN matching_percentage_per_user mp ON r.id = mp.room_id
                                         GROUP BY r.id, r.room_number)
            SELECT *
            FROM final_room_matching
            ORDER BY matching DESC;
            """, nativeQuery = true)
    List<RoomMatching> findBookableRoom(UUID currentUserId, int totalSlot, UUID dormId, int floor);

    @Query("""
    FROM Room r
    JOIN FETCH r.dorm
    JOIN FETCH r.slots
    WHERE r.id = :id
""")
    Room findDetails(UUID id);
}

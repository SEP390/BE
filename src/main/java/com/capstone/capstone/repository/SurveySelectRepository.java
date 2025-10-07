package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.entity.SurveyQuetionSelected;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SurveySelectRepository extends JpaRepository<SurveyQuetionSelected, UUID> {
    @Query("""
        SELECT COUNT(s) > 0
        FROM SurveyQuetionSelected s
        WHERE s.user = :user AND s.surveyOption.surveyQuestion = :question
    """)
    boolean existsByUserAndQuestion(@Param("user") User user, @Param("question") SurveyQuestion question);

    boolean existsByUser(User user);
}

package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SurveyQuetionSelected;
import com.capstone.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SurveySelectRepository extends JpaRepository<SurveyQuetionSelected, UUID> {

    @Query("""
        SELECT CASE WHEN COUNT(s) = (SELECT COUNT(q) FROM SurveyQuestion q)
                    THEN true ELSE false END
        FROM SurveyQuetionSelected s
        WHERE s.user = :user
    """)
    boolean hasCompletedSurvey(User user);

    List<SurveyQuetionSelected> findAllByUser(User user);
}

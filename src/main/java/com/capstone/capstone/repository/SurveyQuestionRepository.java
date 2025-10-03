package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, UUID> {
}

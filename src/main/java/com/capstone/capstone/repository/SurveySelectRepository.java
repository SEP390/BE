package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SurveyQuetionSelected;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurveySelectRepository extends JpaRepository<SurveyQuetionSelected, UUID> {
}

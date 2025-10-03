package com.capstone.capstone.repository;

import com.capstone.capstone.entity.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurveyOptionRepository extends JpaRepository<SurveyOption, UUID> {

}

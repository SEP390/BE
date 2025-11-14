package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyOption.UpdateOptionRequest;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyOption.UpdateOptionResponse;

import java.util.UUID;

public interface ISurveyOptionService {
    UpdateOptionResponse updateOption(UUID id, UpdateOptionRequest request);
}

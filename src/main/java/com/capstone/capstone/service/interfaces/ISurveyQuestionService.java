package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;

public interface ISurveyQuestionService {
    CreateSurveyQuestionResponse createSurveyQuestion(CreateSurveyQuestionRequest request);
}

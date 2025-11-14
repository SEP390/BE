package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.UpdateQuestionRequest;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetQuestionByIdResponse;
import com.capstone.capstone.dto.response.surveyQuestion.UpdateQuestionResponse;

import java.util.List;
import java.util.UUID;

public interface ISurveyQuestionService {
    CreateSurveyQuestionResponse createSurveyQuestion(CreateSurveyQuestionRequest request);

    List<GetAllQuestionResponse> getAllQuestion();

    GetQuestionByIdResponse getQuestionById(UUID id);

    UpdateQuestionResponse updateQuestion(UpdateQuestionRequest request, UUID id);

    CreateSurveyOptionResponse createSurveyOptionForQuestion(CreateSurveyOptionRequest request, UUID questionId);
}

package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.surveySelect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;
import com.capstone.capstone.dto.response.surveySellect.GetAllAnswerSelectedResponse;

import java.util.List;

public interface ISurveySelectService {
    CreateQuestionSelectedResponse createQuestionSelected(CreateQuestionSelectedRequest request);
    List<GetAllAnswerSelectedResponse> getAllAnswerSelected();
}

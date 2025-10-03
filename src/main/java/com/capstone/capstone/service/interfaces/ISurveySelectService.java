package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.SurveySellect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;

public interface ISurveySelectService {
    CreateQuestionSelectedResponse createQuestionSelected(CreateQuestionSelectedRequest request);
}

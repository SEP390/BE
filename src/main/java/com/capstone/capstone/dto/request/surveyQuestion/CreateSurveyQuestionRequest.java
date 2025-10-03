package com.capstone.capstone.dto.request.surveyQuestion;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateSurveyQuestionRequest {
    private String questionContent;
    private List<CreateSurveyOptionRequest> surveyOptions;
}

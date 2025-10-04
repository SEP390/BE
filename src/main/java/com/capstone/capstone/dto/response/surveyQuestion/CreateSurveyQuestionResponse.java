package com.capstone.capstone.dto.response.surveyQuestion;

import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateSurveyQuestionResponse {
    private String questionContent;
    private List<CreateSurveyOptionResponse> surveyOptions;
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.service.interfaces.ISurveyQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class SurveyQuestionService implements ISurveyQuestionService {
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyOptionRepository surveyOptionRepository;

    @Override
    public CreateSurveyQuestionResponse createSurveyQuestion(CreateSurveyQuestionRequest request) {
        SurveyQuestion surveyQuestion = new SurveyQuestion();
        surveyQuestion.setQuestionContent(request.getQuestionContent());
        surveyQuestionRepository.save(surveyQuestion);

        List<SurveyOption> surveyOptions = new ArrayList<>();
        for (CreateSurveyOptionRequest createSurveyOptionRequest : request.getSurveyOptions()) {
            SurveyOption surveyOption = new SurveyOption();
            surveyOption.setOptionContent(createSurveyOptionRequest.getOptionName());
            surveyOption.setSurveyQuestion(surveyQuestion);
            surveyOptions.add(surveyOption);
            surveyOptionRepository.save(surveyOption);
        }
        surveyQuestion.setSurveyOptions(surveyOptions);

        CreateSurveyQuestionResponse createSurveyQuestionResponse = new CreateSurveyQuestionResponse();
        createSurveyQuestionResponse.setQuestionContent(surveyQuestion.getQuestionContent());
        List<CreateSurveyOptionResponse> createSurveyOptionRespons = surveyQuestion.getSurveyOptions()
                .stream()
                .map(option -> {
                    CreateSurveyOptionResponse response = new CreateSurveyOptionResponse();
                    response.setSurveyOption(option.getOptionContent());
                    return response;
                })
                .collect(Collectors.toList());
        createSurveyQuestionResponse.setSurveyOptions(createSurveyOptionRespons);
        return createSurveyQuestionResponse;
    }
}

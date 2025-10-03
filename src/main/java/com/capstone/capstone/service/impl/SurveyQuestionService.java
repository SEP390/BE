package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.UpdateQuestionRequest;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyOption.GetOptionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetQuestionByIdResponse;
import com.capstone.capstone.dto.response.surveyQuestion.UpdateQuestionResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.service.interfaces.ISurveyQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    @Override
    public List<GetAllQuestionResponse> getAllQuestion() {
        List<SurveyQuestion> surveyQuestions = surveyQuestionRepository.findAll();
        List<GetAllQuestionResponse> getAllQuestionResponse = surveyQuestions.stream().map(surveyQuestion -> {
            GetAllQuestionResponse response = new GetAllQuestionResponse();
            response.setId(surveyQuestion.getId());
            response.setQuestionContent(surveyQuestion.getQuestionContent());
            return response;
        }).collect(Collectors.toList());
        return getAllQuestionResponse;
    }

    @Override
    public GetQuestionByIdResponse getQuestionById(UUID id) {
        SurveyQuestion surveyQuestion = surveyQuestionRepository.findById(id).orElseThrow(() -> new NotFoundException("Survey Question Not Found"));
        GetQuestionByIdResponse response = new GetQuestionByIdResponse();
        response.setId(surveyQuestion.getId());
        response.setQuestionContent(surveyQuestion.getQuestionContent());
        response.setOptions(surveyQuestion.getSurveyOptions().stream().map(option -> {
            GetOptionResponse responseOption = new GetOptionResponse();
            responseOption.setId(option.getId());
            responseOption.setOptionContent(option.getOptionContent());
            return responseOption;
        }).collect(Collectors.toList()));
        return response;
    }

    @Override
    public UpdateQuestionResponse updateQuestion(UpdateQuestionRequest request, UUID id) {
        SurveyQuestion surveyQuestion = surveyQuestionRepository.findById(id).orElseThrow(() -> new NotFoundException("Survey Question Not Found"));
        surveyQuestion.setQuestionContent(request.getQuestionContent());
        surveyQuestionRepository.save(surveyQuestion);
        UpdateQuestionResponse response = new UpdateQuestionResponse();
        response.setId(surveyQuestion.getId());
        response.setQuestionContent(surveyQuestion.getQuestionContent());
        return response;
    }
}

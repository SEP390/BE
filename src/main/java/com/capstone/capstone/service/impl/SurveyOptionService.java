package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyOption.UpdateOptionRequest;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyOption.UpdateOptionResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.service.interfaces.ISurveyOptionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyOptionService implements ISurveyOptionService {
    private final SurveyOptionRepository repository;
    private final SurveyQuestionRepository surveyQuestionRepository;

    @Override
    public UpdateOptionResponse updateOption(UUID id, UpdateOptionRequest request) {
        SurveyOption surveyOption = repository.findById(id).orElseThrow(()-> new NotFoundException("Survey option not found"));
        surveyOption.setOptionContent(request.getContent());
        repository.save(surveyOption);
        UpdateOptionResponse response = new UpdateOptionResponse();
        response.setId(surveyOption.getId());
        response.setOptionContent(surveyOption.getOptionContent());
        return response;
    }

}
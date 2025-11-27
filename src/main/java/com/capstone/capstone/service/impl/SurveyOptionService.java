package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveyOption.UpdateOptionRequest;
import com.capstone.capstone.dto.response.surveyOption.UpdateOptionResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.service.interfaces.ISurveyOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyOptionService implements ISurveyOptionService {

    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;

    @Override
    public UpdateOptionResponse updateOption(UUID id, UpdateOptionRequest request) {
        SurveyOption surveyOption = surveyOptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Survey option not found"));
        if (request.getContent() == null) {
            throw new IllegalArgumentException("Option content cannot be null");
        }
        if (request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Option content cannot be empty or blank");
        }
        surveyOption.setOptionContent(request.getContent());
        surveyOptionRepository.save(surveyOption);

        UpdateOptionResponse response = new UpdateOptionResponse();
        response.setId(surveyOption.getId());
        response.setOptionContent(surveyOption.getOptionContent());
        return response;
    }
}
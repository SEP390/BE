package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.SurveySellect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuetionSelected;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveySelectRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.ISurveySelectService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveySelectService implements ISurveySelectService {
    private final SurveySelectRepository surveySelectRepository;
    private final UserRepository userRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    @Override
    public CreateQuestionSelectedResponse createQuestionSelected(CreateQuestionSelectedRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException("User not found"));
        for (UUID id : request.getIds()){
            SurveyOption option = surveyOptionRepository.findById(id).orElseThrow(()-> new NotFoundException("Survey option not found"));
            SurveyQuetionSelected selected = new SurveyQuetionSelected();
            selected.setSurveyOption(option);
            selected.setUser(user);
            surveySelectRepository.save(selected);
        }
        CreateQuestionSelectedResponse response = new CreateQuestionSelectedResponse();
        response.setIds(request.getIds());
        return response;
    }
}

package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.request.surveySelect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;
import com.capstone.capstone.dto.response.surveySellect.GetAllAnswerSelectedResponse;
import com.capstone.capstone.entity.SurveyOption;
import com.capstone.capstone.entity.SurveyQuestion;
import com.capstone.capstone.entity.SurveyQuetionSelected;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.exception.NotFoundException;
import com.capstone.capstone.repository.SurveyOptionRepository;
import com.capstone.capstone.repository.SurveyQuestionRepository;
import com.capstone.capstone.repository.SurveySelectRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.ISurveySelectService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SurveySelectService implements ISurveySelectService {
    private final SurveySelectRepository surveySelectRepository;
    private final UserRepository userRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;

    @Override
    @Transactional
    public CreateQuestionSelectedResponse createQuestionSelected(CreateQuestionSelectedRequest request) {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        //check xem user ddax lam survey chua
        Boolean hasCompletedSurvey = surveySelectRepository.hasCompletedSurvey(user);

        // 1️⃣ Validate danh sách option tối thiểu phải có 1 phần tử

        if (request.getOptionIds() == null || request.getOptionIds().isEmpty()) {
            throw new BadHttpRequestException("Option list cannot be empty");
        }

        // 2️⃣ Kiểm tra duplicate (1 question chỉ chọn 1 option)
        Map<UUID, SurveyQuetionSelected> questionSelected = new HashMap<>();

        for (UUID id : request.getOptionIds()) {
            SurveyOption option = surveyOptionRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Survey option not found"));

            SurveyQuestion question = option.getSurveyQuestion();
            UUID questionId = question.getId();

            if (questionSelected.containsKey(questionId)) {
                throw new BadHttpRequestException("Survey option already exists");
            }

            SurveyQuetionSelected s = new SurveyQuetionSelected();
            s.setUser(user);
            s.setSurveyOption(option);

            questionSelected.put(questionId, s);
        }

        //nếu đã làm survey rồi bà list select pas hêt toàn bộ thì xoá các select cũ và add các select mới
        List<SurveyQuetionSelected> listSelect =  surveySelectRepository.findAllByUser(user);
        if(hasCompletedSurvey){
            surveySelectRepository.deleteAll(listSelect);
        }
        // 3️⃣ Lưu tất cả
        surveySelectRepository.saveAll(questionSelected.values());

        // 4️⃣ Build response
        CreateQuestionSelectedResponse response = new CreateQuestionSelectedResponse();
        response.setIds(request.getOptionIds());
        response.setHasCompletedSurvey(hasCompletedSurvey);

        return response;
    }

    @Override
    public List<GetAllAnswerSelectedResponse> getAllAnswerSelected() {
        UUID userId = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(()-> new NotFoundException("User not found"));
        List<GetAllAnswerSelectedResponse> response = new ArrayList<>();
        boolean hasCompletedSurvey = surveySelectRepository.hasCompletedSurvey(user);
        if (!hasCompletedSurvey){
            throw new BadHttpRequestException("Survey option not found");
        } else {
            List<SurveyQuetionSelected> selecteds = surveySelectRepository.findAllByUser(user);
            for (SurveyQuetionSelected surveyQuetionSelected : selecteds) {
                GetAllAnswerSelectedResponse r = new GetAllAnswerSelectedResponse();
                r.setQuestionId(surveyQuetionSelected.getSurveyOption().getSurveyQuestion().getId());
                r.setQuestionContent(surveyQuetionSelected.getSurveyOption().getSurveyQuestion().getQuestionContent());
                r.setOptionSelectedId(surveyQuetionSelected.getSurveyOption().getId());
                r.setOptionSelected(surveyQuetionSelected.getSurveyOption().getOptionContent());
                response.add(r);
            }
        }
        return response;
    }
}

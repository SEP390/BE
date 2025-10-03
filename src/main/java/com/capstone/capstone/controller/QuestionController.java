package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;
import com.capstone.capstone.service.interfaces.ISurveyQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.SURVEY.SURVEY)
public class QuestionController {
    final ISurveyQuestionService surveyQuestionService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateSurveyQuestionResponse>> createQuestion(@RequestBody CreateSurveyQuestionRequest request) {
        CreateSurveyQuestionResponse createSurveyQuestionResponse = surveyQuestionService.createSurveyQuestion(request);
        BaseResponse<CreateSurveyQuestionResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Create Question Successfully");
        baseResponse.setData(createSurveyQuestionResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }


}

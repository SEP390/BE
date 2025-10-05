package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.SurveySellect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;
import com.capstone.capstone.entity.BaseEntity;
import com.capstone.capstone.service.interfaces.ISurveySelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.SURVEY_SELECT.SURVEY_SELECT)
public class QuestionSelectController {
    private final ISurveySelectService surveySelectService;

    @PostMapping
    public ResponseEntity<BaseResponse<CreateQuestionSelectedResponse>> createQuestionSelected(@RequestBody CreateQuestionSelectedRequest request) {
        CreateQuestionSelectedResponse response = surveySelectService.createQuestionSelected(request);
        BaseResponse<CreateQuestionSelectedResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setData(response);
        baseResponse.setMessage("Question Selected Successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }
}

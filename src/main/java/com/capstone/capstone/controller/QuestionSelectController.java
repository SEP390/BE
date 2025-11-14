package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.surveySelect.CreateQuestionSelectedRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.surveySellect.CreateQuestionSelectedResponse;
import com.capstone.capstone.dto.response.surveySellect.GetAllAnswerSelectedResponse;
import com.capstone.capstone.entity.SurveyQuetionSelected;
import com.capstone.capstone.service.interfaces.ISurveySelectService;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping(ApiConstant.SURVEY_SELECT.ANSWER_SELECTED)
    public ResponseEntity<BaseResponse<List<GetAllAnswerSelectedResponse>>>  getAllQuestionSelected() {
        BaseResponse<List<GetAllAnswerSelectedResponse>> baseResponse = new BaseResponse<>();
        List<GetAllAnswerSelectedResponse> response = surveySelectService.getAllAnswerSelected();
        baseResponse.setData(response);
        baseResponse.setMessage("Answer Selected Successfully");
        baseResponse.setStatus(HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

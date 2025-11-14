package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.CreateSurveyQuestionRequest;
import com.capstone.capstone.dto.request.surveyQuestion.UpdateQuestionRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.CreateSurveyQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetQuestionByIdResponse;
import com.capstone.capstone.dto.response.surveyQuestion.UpdateQuestionResponse;
import com.capstone.capstone.service.interfaces.ISurveyQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.SURVEY.SURVEY)
public class QuestionController {
    final ISurveyQuestionService surveyQuestionService;

    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    public ResponseEntity<BaseResponse<CreateSurveyQuestionResponse>> createQuestion(@RequestBody CreateSurveyQuestionRequest request) {
        CreateSurveyQuestionResponse createSurveyQuestionResponse = surveyQuestionService.createSurveyQuestion(request);
        BaseResponse<CreateSurveyQuestionResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.CREATED.value());
        baseResponse.setMessage("Create Question Successfully");
        baseResponse.setData(createSurveyQuestionResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(baseResponse);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<GetAllQuestionResponse>>> getAllQuestion() {
        List<GetAllQuestionResponse> response = surveyQuestionService.getAllQuestion();
        BaseResponse<List<GetAllQuestionResponse>> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get All Question Successfully");
        baseResponse.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @GetMapping(ApiConstant.SURVEY.GET_BY_ID)
    public ResponseEntity<BaseResponse<GetQuestionByIdResponse>> getQuestionById(@PathVariable UUID id) {
        GetQuestionByIdResponse getQuestionByIdResponse = surveyQuestionService.getQuestionById(id);
        BaseResponse<GetQuestionByIdResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Get Question Successfully");
        baseResponse.setData(getQuestionByIdResponse);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @PutMapping(ApiConstant.SURVEY.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateQuestionResponse>> updateQuestionById(@PathVariable UUID id, @RequestBody UpdateQuestionRequest request) {
        UpdateQuestionResponse response = surveyQuestionService.updateQuestion(request, id);
        BaseResponse<UpdateQuestionResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Update Question Successfully");
        baseResponse.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }

    @PostMapping(ApiConstant.SURVEY.CREATE_OPTIONS)
    public ResponseEntity<BaseResponse<CreateSurveyOptionResponse>> createSurveyQuestion(@RequestBody CreateSurveyOptionRequest request, @PathVariable UUID id) {
        CreateSurveyOptionResponse response = surveyQuestionService.createSurveyOptionForQuestion(request, id);
        BaseResponse<CreateSurveyOptionResponse> baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Create Question Successfully");
        baseResponse.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

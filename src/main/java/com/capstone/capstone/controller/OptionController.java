package com.capstone.capstone.controller;

import com.capstone.capstone.constant.ApiConstant;
import com.capstone.capstone.dto.request.surveyOption.CreateSurveyOptionRequest;
import com.capstone.capstone.dto.request.surveyOption.UpdateOptionRequest;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.surveyOption.CreateSurveyOptionResponse;
import com.capstone.capstone.dto.response.surveyOption.UpdateOptionResponse;
import com.capstone.capstone.service.interfaces.ISurveyOptionService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstant.SURVEY_OPTIONS.SURVEY_OPTIONS)
public class OptionController {
    private final ISurveyOptionService surveyOptionService;

    @PutMapping(ApiConstant.SURVEY_OPTIONS.GET_BY_ID)
    public ResponseEntity<BaseResponse<UpdateOptionResponse>> updateSurveyOptions(@PathVariable UUID id, @RequestBody UpdateOptionRequest request) throws BadRequestException {
        UpdateOptionResponse response = surveyOptionService.updateOption(id, request);
        BaseResponse<UpdateOptionResponse>  baseResponse = new BaseResponse<>();
        baseResponse.setStatus(HttpStatus.OK.value());
        baseResponse.setMessage("Survey option updated successfully");
        baseResponse.setData(response);
        return ResponseEntity.status(HttpStatus.OK).body(baseResponse);
    }
}

package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.dto.response.request.GetAllRequestResponse;
import com.capstone.capstone.dto.response.request.GetRequestByIdResponse;
import com.capstone.capstone.dto.response.request.UpdateRequestResponse;
import com.capstone.capstone.dto.response.surveyQuestion.GetAllQuestionResponse;
import com.capstone.capstone.entity.Request;

import java.util.List;
import java.util.UUID;

public interface IRequestService {
    CreateRequestResponse createRequest(CreateRequestRequest request);
    UpdateRequestResponse updateRequest(UpdateRequestRequest request, UUID id);
    GetRequestByIdResponse getRequestById(UUID id);
    List<GetAllRequestResponse> getAllRequest();
}

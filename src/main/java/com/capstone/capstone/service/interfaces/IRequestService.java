package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.enums.RequestTypeEnum;
import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.request.request.UpdateRequestRequest;
import com.capstone.capstone.dto.response.request.*;

import java.util.List;
import java.util.UUID;

public interface IRequestService {
    CreateRequestResponse createRequest(CreateRequestRequest request);
    UpdateRequestResponse updateRequest(UpdateRequestRequest request, UUID id);
    GetRequestByIdResponse getRequestById(UUID requestId);
    List<GetAllRequestResponse> getAllRequest();
    List<GetAllAnonymousRequestResponse> getAllAnonymousRequest();
}

package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.request.CreateRequestRequest;
import com.capstone.capstone.dto.response.request.CreateRequestResponse;
import com.capstone.capstone.entity.Request;

public interface IRequestService {
    CreateRequestResponse createRequest(CreateRequestRequest request);
}

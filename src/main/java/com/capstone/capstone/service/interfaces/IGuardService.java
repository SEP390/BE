package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.guard.CreateGuardRequest;
import com.capstone.capstone.dto.response.guard.CreateGuardResponse;

public interface IGuardService {
    CreateGuardResponse createGuard(CreateGuardRequest request);
}

package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.auth.AuthRequest;
import com.capstone.capstone.dto.response.auth.AuthResponse;

public interface IAuthService {
    AuthResponse login(AuthRequest authRequest);
}

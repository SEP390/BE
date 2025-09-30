package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.user.RegisterUserRequest;
import com.capstone.capstone.dto.response.user.RegisterUserResponse;

public interface IUserService {
    RegisterUserResponse register(RegisterUserRequest registerUserRequest);
}

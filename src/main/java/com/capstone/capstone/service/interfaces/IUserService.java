package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.user.ProfileUserRequest;
import com.capstone.capstone.dto.request.user.RegisterUserRequest;
import com.capstone.capstone.dto.response.user.ProfileUserResponse;
import com.capstone.capstone.dto.response.user.RegisterUserResponse;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    RegisterUserResponse register(RegisterUserRequest registerUserRequest);
    ProfileUserResponse getById (UUID id);
    List<ProfileUserResponse> getALl();
}

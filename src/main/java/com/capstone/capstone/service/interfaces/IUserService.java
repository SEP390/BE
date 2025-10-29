package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.user.CreateUserRequest;
import com.capstone.capstone.dto.response.user.CreateAccountResponse;
import com.capstone.capstone.dto.response.user.GetAllResidentResponse;
import com.capstone.capstone.dto.response.user.GetUserByIdResponse;
import com.capstone.capstone.dto.response.user.GetUserInformationResponse;

import java.util.List;
import java.util.UUID;

public interface IUserService {
    CreateAccountResponse createAccount(CreateUserRequest createUserRequest);
    GetUserInformationResponse getCurrentUserInformation();
    List<GetAllResidentResponse> getAllResidents();
    GetUserByIdResponse getUserById(UUID userID);
}

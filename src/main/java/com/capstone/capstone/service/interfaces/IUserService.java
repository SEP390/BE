package com.capstone.capstone.service.interfaces;

import com.capstone.capstone.dto.request.user.CreateUserRequest;
import com.capstone.capstone.dto.response.user.CreateAccountResponse;
import com.capstone.capstone.dto.response.user.GetUserInformationResponse;

public interface IUserService {
    CreateAccountResponse createAccount(CreateUserRequest createUserRequest);
    GetUserInformationResponse getCurrentUserInformation();
}

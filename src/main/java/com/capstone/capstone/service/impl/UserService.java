package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.user.RegisterUserRequest;
import com.capstone.capstone.dto.response.user.RegisterUserResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterUserResponse register(RegisterUserRequest registerUserRequest) {
        User user = new User();
        user.setUsername(registerUserRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));
        user.setRole(RoleEnum.RESIDENT);
        user.setEmail(registerUserRequest.getEmail());
        user.setGender(registerUserRequest.getGender());
        user.setDob(registerUserRequest.getDob());
        userRepository.save(user);
        RegisterUserResponse registerUserResponse = new RegisterUserResponse();
        registerUserResponse.setUsername(user.getUsername());
        registerUserResponse.setEmail(user.getEmail());
        registerUserResponse.setDob(user.getDob());
        return registerUserResponse;
    }
}

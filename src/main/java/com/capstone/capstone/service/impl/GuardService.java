package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.guard.CreateGuardRequest;
import com.capstone.capstone.dto.response.guard.CreateGuardResponse;
import com.capstone.capstone.entity.Guard;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.repository.DormRepository;
import com.capstone.capstone.repository.GuardRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IGuardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuardService implements IGuardService {

    private final GuardRepository guardRepository;
    private final UserRepository userRepository;
    private final DormRepository dormRepository;

    @Override
    public CreateGuardResponse createGuard(CreateGuardRequest request) {
        List<User> users = userRepository.findAll();
        if (users.stream().anyMatch(user -> user.getUsername().equals(request.getUsername()))) {
            throw new BadHttpRequestException("Username is already taken");
        }
        if (users.stream().anyMatch(user -> user.getEmail().equals(request.getEmail()))) {
            throw new BadHttpRequestException("Email is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        user.setRole(RoleEnum.GUARD);
        userRepository.save(user);
        Guard guard = new Guard();
        guard.setUser(user);
        guardRepository.save(guard);
        CreateGuardResponse response = new CreateGuardResponse();
        response.setEmail(request.getEmail());
        response.setUsername(request.getUsername());
        return  response;
    }
}

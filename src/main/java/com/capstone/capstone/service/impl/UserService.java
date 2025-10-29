package com.capstone.capstone.service.impl;

import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.request.user.CreateUserRequest;
import com.capstone.capstone.dto.response.user.CreateAccountResponse;
import com.capstone.capstone.dto.response.user.GetAllResidentResponse;
import com.capstone.capstone.dto.response.user.GetUserByIdResponse;
import com.capstone.capstone.dto.response.user.GetUserInformationResponse;
import com.capstone.capstone.entity.Slot;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.exception.BadHttpRequestException;
import com.capstone.capstone.repository.EmployeeRepository;
import com.capstone.capstone.repository.SlotRepository;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.service.interfaces.IUserService;
import com.capstone.capstone.util.AuthenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SlotRepository  slotRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public CreateAccountResponse createAccount(CreateUserRequest createUserRequest) {
        List<User> users = userRepository.findAll();
        if (users.stream().anyMatch(user -> user.getUsername().equals(createUserRequest.getUsername()))) {
            throw new BadHttpRequestException("Username is already taken");
        }
        if (users.stream().anyMatch(user -> user.getEmail().equals(createUserRequest.getEmail()))) {
            throw new BadHttpRequestException("Email is already taken");
        }
        User user = new User();
        user.setUsername(createUserRequest.getUsername());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setRole(createUserRequest.getRole());
        user.setEmail(createUserRequest.getEmail());
        user.setGender(createUserRequest.getGender());
        user.setDob(createUserRequest.getDob());
        userRepository.save(user);
        CreateAccountResponse createAccountResponse = new CreateAccountResponse();
        createAccountResponse.setUsername(user.getUsername());
        createAccountResponse.setEmail(user.getEmail());
        createAccountResponse.setDob(user.getDob());
        return createAccountResponse;
    }

    @Override
    public GetUserInformationResponse getCurrentUserInformation() {
        UUID id = AuthenUtil.getCurrentUserId();
        User user = userRepository.findById(id).orElseThrow(() -> new BadHttpRequestException("User not found"));
        Slot slot = slotRepository.findByUser(user);
        GetUserInformationResponse getUserInformationResponse = new GetUserInformationResponse();
        getUserInformationResponse.setUsername(user.getUsername());
        getUserInformationResponse.setEmail(user.getEmail());
        getUserInformationResponse.setDob(user.getDob());
        getUserInformationResponse.setGender(user.getGender());
        getUserInformationResponse.setStudentId(user.getUserCode());
        getUserInformationResponse.setSlotName(slot == null ? null : slot.getSlotName());
        return getUserInformationResponse;
    }

    @Override
    public List<GetAllResidentResponse> getAllResidents() {
        List<User> residents = userRepository.findUserByRole(RoleEnum.RESIDENT);
        List<GetAllResidentResponse> responses = new ArrayList<>();
        for (User user : residents) {
            GetAllResidentResponse getAllResidentResponse = new GetAllResidentResponse();
            getAllResidentResponse.setResidentId(user.getId());
            getAllResidentResponse.setUserName(user.getUsername());
            getAllResidentResponse.setEmail(user.getEmail());
            getAllResidentResponse.setFullName(user.getFullName());
            getAllResidentResponse.setPhoneNumber(user.getPhoneNumber());
            responses.add(getAllResidentResponse);
        }
        return responses;
    }

    @Override
    public GetUserByIdResponse getUserById(UUID userID) {
        User responseUser = userRepository.findById(userID).orElseThrow(() -> new BadHttpRequestException("User not found"));
        GetUserByIdResponse getUserByIdResponse = new GetUserByIdResponse();
        getUserByIdResponse.setUserID(userID);
        getUserByIdResponse.setUsername(responseUser.getUsername());
        getUserByIdResponse.setFullName(responseUser.getFullName());
        getUserByIdResponse.setEmail(responseUser.getEmail());
        getUserByIdResponse.setDob(responseUser.getDob());
        getUserByIdResponse.setUserCode(responseUser.getUserCode());
        getUserByIdResponse.setPhoneNumber(responseUser.getPhoneNumber());
        getUserByIdResponse.setGender(responseUser.getGender());
        getUserByIdResponse.setRole(responseUser.getRole());
        return getUserByIdResponse;
    }
}

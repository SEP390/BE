package com.capstone.capstone.controller;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import com.capstone.capstone.dto.response.BaseResponse;
import com.capstone.capstone.dto.response.user.CoreUserResponse;
import com.capstone.capstone.entity.User;
import com.capstone.capstone.repository.UserRepository;
import com.capstone.capstone.util.SpecQuery;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class ResidentController {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/api/residents/search")
    public BaseResponse<PagedModel<CoreUserResponse>> search(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) LocalDate dob,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) GenderEnum gender,
            @PageableDefault Pageable pageable
    ) {
        var builder = new SpecQuery<User>();
        builder.like("username", username);
        builder.equal("id", id);
        builder.equal(r -> r.get("slot").get("room").get("id"), roomId);
        builder.like("fullName", fullName);
        builder.like("userCode", userCode);
        builder.like("phoneNumber", phoneNumber);
        builder.like("email", email);
        builder.equal("gender", gender);
        builder.equal("dob", dob);
        builder.equal("role", RoleEnum.RESIDENT);
        return new BaseResponse<>(new PagedModel<>(userRepository.findAll(builder.and(), pageable).map(user -> modelMapper.map(user, CoreUserResponse.class))));
    }
}

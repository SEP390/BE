package com.capstone.capstone.dto.response.user;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CoreUserResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private LocalDate dob;
    private String userCode;
    private String phoneNumber;
    private GenderEnum gender;
    private RoleEnum role;
}

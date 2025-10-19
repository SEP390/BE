package com.capstone.capstone.dto.request.guard;

import com.capstone.capstone.dto.enums.GenderEnum;
import com.capstone.capstone.dto.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateGuardRequest {
    private String username;
    private String password;
    private String userCode;
    private String email;
    private Date dob;
    private GenderEnum gender;
}

package com.capstone.capstone.dto.request.user;

import com.capstone.capstone.dto.enums.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterUserRequest {
    private String username;
    private String password;
    private String email;
    private Date dob;
    private GenderEnum gender;
}

package com.capstone.capstone.dto.request.employee;

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
public class CreateEmployeeRequest {
    private String username;
    private String password;
    private String userCode;
    private String email;
    private Date dob;
    private GenderEnum gender;
}
